package com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.response;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.domain.anuncio.model.Valor;
import com.psnf.api.psnf.domain.anuncio.model.Variacao;
import com.psnf.api.psnf.domain.avaliacao.model.ResumoAvaliacoes;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.CaracteristicaDto;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.InformacoesProdutoDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AnuncioResponse(
        UUID id,
        String titulo,
        String descricao,
        boolean ativo,
        UUID categoriaId,
        UUID vendedorId,
        BigDecimal precoMenor,
        LocalDateTime dataCadastro,
        List<ValorResponse> valores,
        List<ImagemResponse> imagens,
        List<VariacaoResponse> variacoes,
        InformacoesProdutoDto informacoes,
        List<CaracteristicaDto> caracteristicas,
        /** média das avaliações dos compradores (1 casa); null se ainda não há avaliações */
        BigDecimal notaMedia,
        int quantidadeAvaliacoes
) {

    public record ValorResponse(UUID id, BigDecimal valorUnidade, int quantidadeMinima) {
        static ValorResponse from(Valor v) { return new ValorResponse(v.getId(), v.getValorUnidade(), v.getQuantidadeMinima()); }
    }

    public record ImagemResponse(UUID id, String caminho, boolean principal) {
        static ImagemResponse from(ImagemAnuncio i) { return new ImagemResponse(i.getId(), i.getCaminho(), i.isPrincipal()); }
    }

    public record VariacaoResponse(UUID id, String variacao, String sku, String gtin) {
        static VariacaoResponse from(Variacao v) { return new VariacaoResponse(v.getId(), v.getVariacao(), v.getSku(), v.getGtin()); }
    }

    public static AnuncioResponse from(Anuncio anuncio) {
        return from(anuncio, ResumoAvaliacoes.VAZIO);
    }

    public static AnuncioResponse from(Anuncio anuncio, ResumoAvaliacoes avaliacoes) {
        ResumoAvaliacoes r = avaliacoes == null ? ResumoAvaliacoes.VAZIO : avaliacoes;
        return new AnuncioResponse(
                anuncio.getId(),
                anuncio.getTitulo(),
                anuncio.getDescricao(),
                anuncio.isAtivo(),
                anuncio.getCategoria(),
                anuncio.getVendedor(),
                anuncio.getMenorValor(),
                anuncio.getDataCadastro(),
                anuncio.getValores().stream().map(ValorResponse::from).toList(),
                anuncio.getImagens().stream().map(ImagemResponse::from).toList(),
                anuncio.getVariacoes().stream().map(VariacaoResponse::from).toList(),
                InformacoesProdutoDto.from(anuncio.getInformacoes()),
                anuncio.getCaracteristicas().stream().map(CaracteristicaDto::from).toList(),
                r.media(),
                r.total()
        );
    }
}
