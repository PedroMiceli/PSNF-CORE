package com.psnf.api.psnf.infrastructure.web.controller.negociacao;

import com.psnf.api.psnf.domain.negociacao.port.in.ConsultarNegociacaoUseCase;
import com.psnf.api.psnf.domain.negociacao.port.in.EnviarMensagemNegociacaoUseCase;
import com.psnf.api.psnf.domain.negociacao.port.in.IniciarNegociacaoUseCase;
import com.psnf.api.psnf.infrastructure.web.controller.UsuarioAutenticadoUtil;
import com.psnf.api.psnf.infrastructure.web.controller.negociacao.dto.IniciarNegociacaoRequest;
import com.psnf.api.psnf.infrastructure.web.controller.negociacao.dto.MensagemNegociacaoRequest;
import com.psnf.api.psnf.infrastructure.web.controller.negociacao.dto.NegociacaoResponse;
import com.psnf.api.psnf.infrastructure.web.controller.negociacao.dto.NegociacaoResumoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Chat de negociação entre comprador e vendedor sobre um anúncio.
 * O comprador (usuário logado) inicia a partir do anúncio; depois os dois trocam mensagens.
 */
@RestController
@RequestMapping("/negociacoes")
@RequiredArgsConstructor
public class NegociacaoController {

    private final IniciarNegociacaoUseCase iniciarNegociacaoUseCase;
    private final ConsultarNegociacaoUseCase consultarNegociacaoUseCase;
    private final EnviarMensagemNegociacaoUseCase enviarMensagemNegociacaoUseCase;

    /** 201 quando a negociação é criada; 200 quando o comprador já tinha uma para este anúncio. */
    @PostMapping
    public ResponseEntity<NegociacaoResponse> iniciar(@RequestBody @Valid IniciarNegociacaoRequest request) {
        var r = iniciarNegociacaoUseCase.iniciar(request.anuncioId(), usuarioLogado());
        NegociacaoResponse body = NegociacaoResponse.from(r.detalhes());
        return r.nova() ? ResponseEntity.created(URI.create("/negociacoes/" + body.id())).body(body) : ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<List<NegociacaoResumoResponse>> minhas() {
        UUID usuario = usuarioLogado();
        return ResponseEntity.ok(consultarNegociacaoUseCase.listarDoUsuario(usuario).stream()
                .map(d -> NegociacaoResumoResponse.from(d, usuario)).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NegociacaoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(NegociacaoResponse.from(consultarNegociacaoUseCase.buscarPorId(id, usuarioLogado())));
    }

    @PostMapping("/{id}/mensagens")
    public ResponseEntity<NegociacaoResponse> enviarMensagem(@PathVariable UUID id, @RequestBody @Valid MensagemNegociacaoRequest request) {
        return ResponseEntity.ok(NegociacaoResponse.from(enviarMensagemNegociacaoUseCase.enviar(id, usuarioLogado(), request.texto())));
    }

    private UUID usuarioLogado() {
        return UsuarioAutenticadoUtil.obterUsuarioLogadoId();
    }
}
