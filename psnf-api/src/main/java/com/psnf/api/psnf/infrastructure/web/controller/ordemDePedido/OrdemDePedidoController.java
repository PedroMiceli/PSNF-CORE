package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.*;
import com.psnf.api.psnf.infrastructure.web.controller.UsuarioAutenticadoUtil;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request.*;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.response.AlertaMensagemResponse;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.response.OrdemDePedidoResponse;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.response.OrdemDePedidoResumoResponse;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.mapper.OrdemDePedidoRequestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ordens-de-pedido")
@RequiredArgsConstructor
public class OrdemDePedidoController {

    private final CadastrarOrdemDePedidoUseCase cadastrarOrdemDePedidoUseCase;
    private final BuscarDetalhesOrdemDePedidoUseCase buscarDetalhesOrdemDePedidoUseCase;
    private final ListarOrdensDePedidoUseCase listarOrdensDePedidoUseCase;
    private final RegistrarPagamentoUseCase registrarPagamentoUseCase;
    private final AdicionarNotaFiscalUseCase adicionarNotaFiscalUseCase;
    private final RegistrarMensagemIncidenteUseCase registrarMensagemIncidenteUseCase;
    private final AlterarStatusOrdemDePedidoUseCase alterarStatusOrdemDePedidoUseCase;
    private final com.psnf.api.psnf.domain.ordemDePedido.port.in.AlterarValoresOrdemUseCase alterarValoresOrdemUseCase;

    /**
     * O VENDEDOR (usuário logado) cria a ordem para o comprador com quem fechou acordo no chat,
     * informando os itens com a quantidade e o valor unitário negociado.
     */
    @PostMapping
    public ResponseEntity<OrdemDePedidoResponse> salvarOrdemDePedido(@RequestBody @Valid OrdemDePedidoRequest request) {
        OrdemDePedido ordem = cadastrarOrdemDePedidoUseCase.cadastrarOrdemDePedido(
                OrdemDePedidoRequestMapper.toCommand(usuarioLogado(), request));
        return ResponseEntity
                .created(URI.create("/ordens-de-pedido/" + ordem.getId()))
                .body(resposta(ordem));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemDePedidoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(resposta(
                buscarDetalhesOrdemDePedidoUseCase.buscarPorId(id, usuarioLogado())));
    }

    @GetMapping("/compras")
    public ResponseEntity<List<OrdemDePedidoResumoResponse>> minhasCompras() {
        return ResponseEntity.ok(listarOrdensDePedidoUseCase.listarCompras(usuarioLogado()).stream()
                .map(o -> OrdemDePedidoResumoResponse.from(o, usuarioLogado()))
                .toList());
    }

    @GetMapping("/vendas")
    public ResponseEntity<List<OrdemDePedidoResumoResponse>> minhasVendas() {
        return ResponseEntity.ok(listarOrdensDePedidoUseCase.listarVendas(usuarioLogado()).stream()
                .map(o -> OrdemDePedidoResumoResponse.from(o, usuarioLogado()))
                .toList());
    }

    @PostMapping("/{id}/pagamentos")
    public ResponseEntity<OrdemDePedidoResponse> registrarPagamento(@PathVariable UUID id, @RequestBody @Valid PagamentoRequest request) {
        return ResponseEntity.ok(resposta(registrarPagamentoUseCase.registrarPagamento(
                OrdemDePedidoRequestMapper.toCommand(id, usuarioLogado(), request))));
    }

    /** O vendedor exclui uma forma de pagamento cadastrada por engano (antes do comprovante). */
    @DeleteMapping("/{id}/pagamentos/{pagamentoId}")
    public ResponseEntity<OrdemDePedidoResponse> removerPagamento(@PathVariable UUID id, @PathVariable UUID pagamentoId) {
        return ResponseEntity.ok(resposta(registrarPagamentoUseCase.removerPagamento(id, usuarioLogado(), pagamentoId)));
    }

    /** O comprador anexa (ou troca) o comprovante de um pagamento. */
    @PostMapping("/{id}/pagamentos/{pagamentoId}/comprovante")
    public ResponseEntity<OrdemDePedidoResponse> anexarComprovante(@PathVariable UUID id, @PathVariable UUID pagamentoId,
                                                                   @RequestBody @Valid ComprovanteRequest request) {
        return ResponseEntity.ok(resposta(
                registrarPagamentoUseCase.anexarComprovante(id, usuarioLogado(), pagamentoId, request.caminho())));
    }

    /** O comprador confere produtos, quantidades e valores e aceita a ordem. */
    @PatchMapping("/{id}/aceitar")
    public ResponseEntity<OrdemDePedidoResponse> aceitar(@PathVariable UUID id) {
        return ResponseEntity.ok(resposta(alterarStatusOrdemDePedidoUseCase.aceitar(id, usuarioLogado())));
    }

    /** Renegociação: o vendedor altera o valor unitário dos itens enquanto o comprador não aceitou. */
    @PutMapping("/{id}/itens/valores")
    public ResponseEntity<OrdemDePedidoResponse> alterarValores(@PathVariable UUID id, @RequestBody @Valid AlterarValoresRequest request) {
        java.util.Map<UUID, java.math.BigDecimal> valores = new java.util.LinkedHashMap<>();
        request.itens().forEach(i -> valores.put(i.itemId(), i.valorUnidade()));
        return ResponseEntity.ok(resposta(alterarValoresOrdemUseCase.alterarValores(id, usuarioLogado(), valores)));
    }

    @PostMapping("/{id}/notas-fiscais")
    public ResponseEntity<OrdemDePedidoResponse> adicionarNotaFiscal(@PathVariable UUID id, @RequestBody @Valid NotaFiscalRequest request) {
        return ResponseEntity.ok(resposta(adicionarNotaFiscalUseCase.adicionarNotaFiscal(
                OrdemDePedidoRequestMapper.toCommand(id, usuarioLogado(), request))));
    }

    @PostMapping("/{id}/mensagens")
    public ResponseEntity<OrdemDePedidoResponse> registrarMensagem(@PathVariable UUID id, @RequestBody @Valid MensagemIncidenteRequest request) {
        return ResponseEntity.ok(resposta(registrarMensagemIncidenteUseCase.registrarMensagem(
                OrdemDePedidoRequestMapper.toCommand(id, usuarioLogado(), request))));
    }

    @PatchMapping("/{id}/enviar")
    public ResponseEntity<OrdemDePedidoResponse> enviar(@PathVariable UUID id, @RequestBody(required = false) @Valid EnviarOrdemDePedidoRequest request) {
        return ResponseEntity.ok(resposta(alterarStatusOrdemDePedidoUseCase.enviar(id, usuarioLogado(),
                request == null ? null : request.previsaoDeEntrega(),
                request == null ? null : request.descricaoEntrega(),
                request == null ? null : request.codigoRastreio(),
                request == null ? null : request.retiradaNoLocal())));
    }

    /**
     * O vendedor informa ou corrige os detalhes da entrega (descrição, código de envio e retirada no local,
     * todos opcionais). Marcar ou desmarcar a retirada avisa o comprador com uma mensagem automática.
     */
    @PutMapping("/{id}/entrega")
    public ResponseEntity<OrdemDePedidoResponse> registrarEntrega(@PathVariable UUID id, @RequestBody @Valid EntregaRequest request) {
        return ResponseEntity.ok(resposta(alterarStatusOrdemDePedidoUseCase.registrarEntrega(
                id, usuarioLogado(), request.descricao(), request.codigoRastreio(), request.retiradaNoLocal())));
    }

    @PatchMapping("/{id}/confirmar-entrega")
    public ResponseEntity<OrdemDePedidoResponse> confirmarEntrega(@PathVariable UUID id) {
        return ResponseEntity.ok(resposta(
                alterarStatusOrdemDePedidoUseCase.confirmarEntrega(id, usuarioLogado())));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<OrdemDePedidoResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(resposta(
                alterarStatusOrdemDePedidoUseCase.cancelar(id, usuarioLogado())));
    }

    /** Ordens com mensagens de incidente novas para o usuário logado (para os alertas). */
    @GetMapping("/mensagens-nao-lidas")
    public ResponseEntity<List<AlertaMensagemResponse>> mensagensNaoLidas() {
        UUID usuario = usuarioLogado();
        return ResponseEntity.ok(listarOrdensDePedidoUseCase.listarComMensagensNaoLidas(usuario).stream()
                .map(o -> AlertaMensagemResponse.from(o, usuario))
                .sorted(Comparator.comparing(AlertaMensagemResponse::ultimaMensagemEm, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList());
    }

    /** O usuário viu as mensagens de incidente da ordem: deixam de aparecer como novas. */
    @PatchMapping("/{id}/mensagens/lidas")
    public ResponseEntity<OrdemDePedidoResponse> marcarMensagensLidas(@PathVariable UUID id) {
        return ResponseEntity.ok(resposta(registrarMensagemIncidenteUseCase.marcarLidas(id, usuarioLogado())));
    }

    private OrdemDePedidoResponse resposta(OrdemDePedido ordem) {
        return OrdemDePedidoResponse.from(ordem, usuarioLogado());
    }

    private UUID usuarioLogado() {
        return UsuarioAutenticadoUtil.obterUsuarioLogadoId();
    }
}
