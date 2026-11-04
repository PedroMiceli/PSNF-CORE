package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido;

import com.psnf.api.psnf.domain.ordemDePedido.models.OrdemDePedido;
import com.psnf.api.psnf.domain.ordemDePedido.port.in.*;
import com.psnf.api.psnf.infrastructure.web.controller.UsuarioAutenticadoUtil;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request.*;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.response.OrdemDePedidoResponse;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.response.OrdemDePedidoResumoResponse;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.mapper.OrdemDePedidoRequestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @PostMapping
    public ResponseEntity<OrdemDePedidoResponse> salvarOrdemDePedido(@RequestBody @Valid OrdemDePedidoRequest request) {
        OrdemDePedido ordem = cadastrarOrdemDePedidoUseCase.cadastrarOrdemDePedido(
                OrdemDePedidoRequestMapper.toCommand(usuarioLogado(), request));
        return ResponseEntity
                .created(URI.create("/ordens-de-pedido/" + ordem.getId()))
                .body(OrdemDePedidoResponse.from(ordem));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemDePedidoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(OrdemDePedidoResponse.from(
                buscarDetalhesOrdemDePedidoUseCase.buscarPorId(id, usuarioLogado())));
    }

    @GetMapping("/compras")
    public ResponseEntity<List<OrdemDePedidoResumoResponse>> minhasCompras() {
        return ResponseEntity.ok(listarOrdensDePedidoUseCase.listarCompras(usuarioLogado()).stream()
                .map(OrdemDePedidoResumoResponse::from)
                .toList());
    }

    @GetMapping("/vendas")
    public ResponseEntity<List<OrdemDePedidoResumoResponse>> minhasVendas() {
        return ResponseEntity.ok(listarOrdensDePedidoUseCase.listarVendas(usuarioLogado()).stream()
                .map(OrdemDePedidoResumoResponse::from)
                .toList());
    }

    @PostMapping("/{id}/pagamentos")
    public ResponseEntity<OrdemDePedidoResponse> registrarPagamento(@PathVariable UUID id, @RequestBody @Valid PagamentoRequest request) {
        return ResponseEntity.ok(OrdemDePedidoResponse.from(registrarPagamentoUseCase.registrarPagamento(
                OrdemDePedidoRequestMapper.toCommand(id, usuarioLogado(), request))));
    }

    @PostMapping("/{id}/notas-fiscais")
    public ResponseEntity<OrdemDePedidoResponse> adicionarNotaFiscal(@PathVariable UUID id, @RequestBody @Valid NotaFiscalRequest request) {
        return ResponseEntity.ok(OrdemDePedidoResponse.from(adicionarNotaFiscalUseCase.adicionarNotaFiscal(
                OrdemDePedidoRequestMapper.toCommand(id, usuarioLogado(), request))));
    }

    @PostMapping("/{id}/mensagens")
    public ResponseEntity<OrdemDePedidoResponse> registrarMensagem(@PathVariable UUID id, @RequestBody @Valid MensagemIncidenteRequest request) {
        return ResponseEntity.ok(OrdemDePedidoResponse.from(registrarMensagemIncidenteUseCase.registrarMensagem(
                OrdemDePedidoRequestMapper.toCommand(id, usuarioLogado(), request))));
    }

    @PatchMapping("/{id}/enviar")
    public ResponseEntity<OrdemDePedidoResponse> enviar(@PathVariable UUID id, @RequestBody(required = false) @Valid EnviarOrdemDePedidoRequest request) {
        return ResponseEntity.ok(OrdemDePedidoResponse.from(alterarStatusOrdemDePedidoUseCase.enviar(
                id, usuarioLogado(), request == null ? null : request.previsaoDeEntrega())));
    }

    @PatchMapping("/{id}/confirmar-entrega")
    public ResponseEntity<OrdemDePedidoResponse> confirmarEntrega(@PathVariable UUID id) {
        return ResponseEntity.ok(OrdemDePedidoResponse.from(
                alterarStatusOrdemDePedidoUseCase.confirmarEntrega(id, usuarioLogado())));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<OrdemDePedidoResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(OrdemDePedidoResponse.from(
                alterarStatusOrdemDePedidoUseCase.cancelar(id, usuarioLogado())));
    }

    private UUID usuarioLogado() {
        return UsuarioAutenticadoUtil.obterUsuarioLogadoId();
    }
}
