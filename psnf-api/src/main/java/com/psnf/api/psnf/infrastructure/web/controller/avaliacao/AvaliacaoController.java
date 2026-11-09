package com.psnf.api.psnf.infrastructure.web.controller.avaliacao;

import com.psnf.api.psnf.domain.avaliacao.port.in.AvaliarAnuncioUseCase;
import com.psnf.api.psnf.infrastructure.web.controller.UsuarioAutenticadoUtil;
import com.psnf.api.psnf.infrastructure.web.controller.avaliacao.dto.AvaliacaoRequest;
import com.psnf.api.psnf.infrastructure.web.controller.avaliacao.dto.AvaliacoesResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Avaliações dos produtos. Só avalia quem comprou e recebeu o produto (ordem ENTREGUE);
 * cada comprador tem uma avaliação por anúncio ("minha"), que pode editar ou excluir.
 */
@RestController
@RequestMapping("/anuncios/{anuncioId}/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliarAnuncioUseCase avaliarAnuncioUseCase;

    @GetMapping
    public ResponseEntity<AvaliacoesResponse> listar(@PathVariable UUID anuncioId) {
        return ResponseEntity.ok(AvaliacoesResponse.from(avaliarAnuncioUseCase.listar(anuncioId, usuarioLogado())));
    }

    /** Publica ou edita a avaliação do usuário logado. */
    @PutMapping("/minha")
    public ResponseEntity<AvaliacoesResponse> avaliar(@PathVariable UUID anuncioId, @RequestBody @Valid AvaliacaoRequest request) {
        return ResponseEntity.ok(AvaliacoesResponse.from(avaliarAnuncioUseCase.avaliar(
                anuncioId, usuarioLogado(), request.nota(), request.texto(), request.imagens())));
    }

    @DeleteMapping("/minha")
    public ResponseEntity<AvaliacoesResponse> remover(@PathVariable UUID anuncioId) {
        return ResponseEntity.ok(AvaliacoesResponse.from(avaliarAnuncioUseCase.remover(anuncioId, usuarioLogado())));
    }

    private UUID usuarioLogado() {
        return UsuarioAutenticadoUtil.obterUsuarioLogadoId();
    }
}
