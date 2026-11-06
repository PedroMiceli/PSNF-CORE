package com.psnf.api.psnf.domain.anuncio.model;

import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import com.psnf.api.psnf.domain.shared.model.BaseDomain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate root do anúncio. Valores, imagens e variações pertencem ao anúncio;
 * categoria e vendedor são referenciados apenas pelo id (outros aggregates).
 */
public class Anuncio extends BaseDomain {

    private String titulo;
    private String descricao;
    private boolean ativo;
    private UUID categoria;
    private final UUID vendedor;
    private List<Valor> valores;
    private List<ImagemAnuncio> imagens;
    private List<Variacao> variacoes;
    private InformacoesProduto informacoes = InformacoesProduto.VAZIO;
    private List<Caracteristica> caracteristicas = new ArrayList<>();

    private Anuncio(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String titulo, String descricao, boolean ativo, UUID categoria, UUID vendedor, List<Valor> valores, List<ImagemAnuncio> imagens, List<Variacao> variacoes) {
        super(id, dataCadastro, dataAlteracao, dataExcluido);
        this.titulo = titulo;
        this.descricao = descricao;
        this.ativo = ativo;
        this.categoria = categoria;
        this.vendedor = vendedor;
        this.valores = copia(valores);
        this.imagens = copia(imagens);
        this.variacoes = copia(variacoes);
    }

    public static Anuncio criar(String titulo, String descricao, UUID categoria, UUID vendedor, List<Valor> valores, List<ImagemAnuncio> imagens, List<Variacao> variacoes) {
        if (vendedor == null) {
            throw new IllegalArgumentException("Vendedor é obrigatório");
        }
        Anuncio anuncio = new Anuncio(null, LocalDateTime.now(), null, null, titulo, descricao, true, categoria, vendedor, valores, imagens, variacoes);
        anuncio.validar();
        return anuncio;
    }

    public static Anuncio restaurar(UUID id, LocalDateTime dataCadastro, LocalDateTime dataAlteracao, LocalDateTime dataExcluido, String titulo, String descricao, boolean ativo, UUID categoria, UUID vendedor, List<Valor> valores, List<ImagemAnuncio> imagens, List<Variacao> variacoes,
                                    InformacoesProduto informacoes, List<Caracteristica> caracteristicas) {
        Anuncio anuncio = new Anuncio(id, dataCadastro, dataAlteracao, dataExcluido, titulo, descricao, ativo, categoria, vendedor, valores, imagens, variacoes);
        anuncio.informacoes = InformacoesProduto.ouVazio(informacoes);
        anuncio.caracteristicas = copia(caracteristicas);
        return anuncio;
    }

    /******************************************* REGRAS *******************************************/

    public void atualizar(String titulo, String descricao, UUID categoria, List<Valor> valores, List<ImagemAnuncio> imagens, List<Variacao> variacoes) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
        this.valores = copia(valores);
        this.imagens = copia(imagens);
        this.variacoes = copia(variacoes);
        validar();
    }

    /**
     * Informações complementares do produto (todas opcionais) e ficha técnica.
     * Substitui as anteriores; null limpa.
     */
    public void definirDetalhes(InformacoesProduto informacoes, List<Caracteristica> caracteristicas) {
        List<Caracteristica> lista = copia(caracteristicas);
        if (lista.size() > Caracteristica.MAXIMO_POR_ANUNCIO) {
            throw new RegraDeNegocioException("A ficha técnica aceita no máximo " + Caracteristica.MAXIMO_POR_ANUNCIO + " características");
        }
        this.informacoes = InformacoesProduto.ouVazio(informacoes);
        this.caracteristicas = lista;
    }

    /** Informações que vão para o item da ordem de pedido (com SKU/GTIN da variação, se houver). */
    public InformacoesProduto informacoesParaVenda(Variacao variacao) {
        return variacao == null ? informacoes : informacoes.comCodigosDaVariacao(variacao.getSku(), variacao.getGtin());
    }

    public void ativar() { this.ativo = true; }

    public void desativar() { this.ativo = false; }

    public boolean pertenceA(UUID usuarioId) {
        return vendedor.equals(usuarioId);
    }

    /**
     * Valor unitário aplicado para a quantidade informada: usa a faixa com a maior
     * quantidade mínima que ainda seja menor ou igual à quantidade comprada.
     */
    public BigDecimal calcularValorUnidade(int quantidade) {
        return valores.stream()
                .filter(v -> v.getQuantidadeMinima() <= quantidade)
                .max(Comparator.comparingInt(Valor::getQuantidadeMinima))
                .map(Valor::getValorUnidade)
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Quantidade " + quantidade + " abaixo do mínimo (" + getQuantidadeMinima() + ") para o anúncio '" + titulo + "'"));
    }

    public int getQuantidadeMinima() {
        return valores.stream().mapToInt(Valor::getQuantidadeMinima).min().orElse(1);
    }

    public BigDecimal getMenorValor() {
        return valores.stream().map(Valor::getValorUnidade).min(BigDecimal::compareTo).orElse(null);
    }

    public Optional<ImagemAnuncio> getImagemPrincipal() {
        return imagens.stream().filter(ImagemAnuncio::isPrincipal).findFirst();
    }

    public Variacao buscarVariacao(UUID variacaoId) {
        return variacoes.stream()
                .filter(v -> variacaoId.equals(v.getId()))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Variação " + variacaoId + " não pertence ao anúncio '" + titulo + "'"));
    }

    private void validar() {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        if (valores.isEmpty()) {
            throw new RegraDeNegocioException("É necessário informar ao menos um valor");
        }
        long faixasDistintas = valores.stream().map(Valor::getQuantidadeMinima).distinct().count();
        if (faixasDistintas != valores.size()) {
            throw new RegraDeNegocioException("Não pode haver dois valores com a mesma quantidade mínima");
        }
        if (imagens.isEmpty()) {
            throw new RegraDeNegocioException("É necessário informar ao menos uma imagem");
        }
        long principais = imagens.stream().filter(ImagemAnuncio::isPrincipal).count();
        if (principais > 1) {
            throw new RegraDeNegocioException("Apenas uma imagem pode ser a principal");
        }
        if (principais == 0) {
            // se nenhuma foi marcada, a primeira vira a principal
            imagens.getFirst().definirPrincipal(true);
        }
    }

    private static <T> List<T> copia(List<T> lista) {
        return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
    }

    /******************************************* GETTERS *******************************************/

    public List<ImagemAnuncio> getImagens() { return List.copyOf(imagens); }
    public List<Variacao> getVariacoes() { return List.copyOf(variacoes); }
    public List<Valor> getValores() { return List.copyOf(valores); }
    public InformacoesProduto getInformacoes() { return informacoes; }
    public List<Caracteristica> getCaracteristicas() { return List.copyOf(caracteristicas); }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public boolean isAtivo() { return ativo; }
    public UUID getCategoria() { return categoria; }
    public UUID getVendedor() { return vendedor; }
}
