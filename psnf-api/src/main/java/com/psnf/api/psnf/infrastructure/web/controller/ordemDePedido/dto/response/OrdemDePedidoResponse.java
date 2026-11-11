package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.response;

import com.psnf.api.psnf.domain.ordemDePedido.models.*;
import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumStatusOrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.models.enums.EnumTipoPagamento;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.InformacoesProdutoDto;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.response.UsuarioResumoResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrdemDePedidoResponse(
        UUID id,
        String codigo,
        EnumStatusOrdemDePedido status,
        LocalDateTime dataCadastro,
        LocalDate previsaoDeEntrega,
        UsuarioResumoResponse comprador,
        UsuarioResumoResponse vendedor,
        BigDecimal valorTotal,
        /** soma das formas de pagamento cadastradas pelo vendedor */
        BigDecimal valorCadastrado,
        /** soma dos pagamentos com comprovante */
        BigDecimal valorPago,
        /** valor que ainda falta o vendedor cadastrar em formas de pagamento */
        BigDecimal valorRestante,
        /** ainda falta pagar (algum pagamento sem comprovante), mesmo com o pedido enviado */
        boolean pagamentoPendente,
        /** mensagens de incidente do outro lado que o usuário logado ainda não viu */
        int mensagensNaoLidas,
        /** descrição e código de envio informados pelo vendedor; null se ainda não informou */
        EntregaResponse entrega,
        /** negociação (chat) em que a ordem foi combinada; null se foi criada fora do chat */
        UUID negociacaoId,
        List<ItemResponse> itens,
        List<PagamentoResponse> pagamentos,
        List<NotaFiscalResponse> notasFiscais,
        List<MensagemResponse> mensagensIncidente,
        /** fotos/PDFs da entrega (prova de saída ou de retirada) */
        List<AnexoEntregaResponse> anexosEntrega,
        /** quem confirmou a entrega/retirada e quando; null enquanto não confirmada */
        ConfirmacaoResponse confirmacaoEntrega
) {

    /** @param papel COMPRADOR ou VENDEDOR */
    public record Autor(UUID id, String nome, String papel) {
        static Autor de(OrdemDePedido o, UUID id) {
            if (id == null) return null;
            boolean vendedor = o.isVendedor(id);
            return new Autor(id, vendedor ? o.getVendedor().getNome() : o.getComprador().getNome(), vendedor ? "VENDEDOR" : "COMPRADOR");
        }
    }

    public record AnexoEntregaResponse(UUID id, String caminho, Autor enviadoPor, LocalDateTime dataCadastro) {
    }

    public record ConfirmacaoResponse(Autor confirmadaPor, LocalDateTime confirmadaEm) {
    }

    /** informacoes: cópia das informações do produto no momento da compra (para NF, garantia...) */
    public record ItemResponse(UUID id, UUID anuncioId, String tituloAnuncio, String variacao, int quantidade,
                               BigDecimal valorUnidade, BigDecimal subtotal, InformacoesProdutoDto informacoes) {
        static ItemResponse from(ItemPedido i) {
            return new ItemResponse(i.getId(), i.getAnuncioId(), i.getTituloAnuncio(), i.getVariacao(), i.getQuantidade(),
                    i.getValorUnidade(), i.getSubtotal(), InformacoesProdutoDto.from(i.getInformacoes()));
        }
    }

    /** Pagamento com todos os dados (chave PIX, boleto, dados bancários) para a tela de detalhes. */
    public record PagamentoResponse(UUID id, EnumTipoPagamento tipoPagamento, BigDecimal valor, String nomeReferencia,
                                    LocalDate dataReferencia, String chavePix, String boleto, String codigoBanco,
                                    String agencia, String conta, String comprovante, LocalDateTime dataComprovante,
                                    LocalDateTime dataCadastro) {
        static PagamentoResponse from(Pagamento p) {
            return new PagamentoResponse(p.getId(), p.getTipoPagamento(), p.getValor(), p.getNomeReferencia(), p.getDataReferencia(),
                    p.getChavePix(), p.getBoleto(), p.getCodigoBanco(), p.getAgencia(), p.getConta(), p.getComprovante(),
                    p.getDataComprovante(), p.getDataCadastro());
        }
    }

    public record EntregaResponse(UUID id, String descricao, String codigoRastreio, boolean retiradaNoLocal,
                                  LocalDateTime dataCadastro, LocalDateTime dataAlteracao) {
        static EntregaResponse from(Entrega e) {
            return new EntregaResponse(e.getId(), e.getDescricao(), e.getCodigoRastreio(), e.isRetiradaNoLocal(),
                    e.getDataCadastro(), e.getDataAlteracao());
        }
    }

    public record NotaFiscalResponse(UUID id, String caminho, LocalDateTime dataCadastro) {
        static NotaFiscalResponse from(NotaFiscal n) {
            return new NotaFiscalResponse(n.getId(), n.getCaminho(), n.getDataCadastro());
        }
    }

    public record MensagemResponse(UUID id, String mensagem, String imagem, UUID usuarioCriadorId, LocalDateTime dataCadastro) {
        static MensagemResponse from(MensagemIncidente m) {
            return new MensagemResponse(m.getId(), m.getMensagem(), m.getImagem(), m.getUsuarioCriador(), m.getDataCadastro());
        }
    }

    public static OrdemDePedidoResponse from(OrdemDePedido ordem) {
        return from(ordem, null);
    }

    /** @param usuarioLogado para calcular as mensagens não lidas de quem está vendo */
    public static OrdemDePedidoResponse from(OrdemDePedido ordem, UUID usuarioLogado) {
        return new OrdemDePedidoResponse(
                ordem.getId(),
                ordem.getCodigo(),
                ordem.getStatus(),
                ordem.getDataCadastro(),
                ordem.getPrevisaoDeEntrega(),
                UsuarioResumoResponse.from(ordem.getComprador()),
                UsuarioResumoResponse.from(ordem.getVendedor()),
                ordem.getValorTotal(),
                ordem.getValorCadastrado(),
                ordem.getValorPago(),
                ordem.getValorRestante(),
                ordem.isPagamentoPendente(),
                ordem.mensagensIncidenteNaoLidas(usuarioLogado),
                ordem.getEntrega() == null ? null : EntregaResponse.from(ordem.getEntrega()),
                ordem.getNegociacaoId(),
                ordem.getItens().stream().map(ItemResponse::from).toList(),
                ordem.getPagamentos().stream().map(PagamentoResponse::from).toList(),
                ordem.getNotasFiscais().stream().map(NotaFiscalResponse::from).toList(),
                ordem.getMensagensIncidente().stream().map(MensagemResponse::from).toList(),
                ordem.getAnexosEntrega().stream().map(a -> new AnexoEntregaResponse(a.getId(), a.getCaminho(),
                        Autor.de(ordem, a.getEnviadoPor()), a.getDataCadastro())).toList(),
                ordem.getEntregaConfirmadaPor() == null ? null
                        : new ConfirmacaoResponse(Autor.de(ordem, ordem.getEntregaConfirmadaPor()), ordem.getEntregaConfirmadaEm())
        );
    }
}
