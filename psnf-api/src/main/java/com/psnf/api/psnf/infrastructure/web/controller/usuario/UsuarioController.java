package com.psnf.api.psnf.infrastructure.web.controller.usuario;

import com.psnf.api.psnf.domain.anuncio.port.in.ListarAnunciosPorVendedorUseCase;
import com.psnf.api.psnf.domain.auditoria.port.in.ConsultarAtividadeUseCase;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.AtualizarUsuarioUseCase;
import com.psnf.api.psnf.domain.usuario.port.in.DetalhesUsuarioUseCase;
import com.psnf.api.psnf.domain.usuario.port.in.GerenciarEnderecoUseCase;
import com.psnf.api.psnf.domain.usuario.port.in.SalvarUsuarioUseCase;
import com.psnf.api.psnf.infrastructure.web.controller.UsuarioAutenticadoUtil;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.response.AnuncioResponse;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request.AtualizarUsuarioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request.EnderecoRequest;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request.UsuarioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.response.UsuarioResponse;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.mapper.UsuarioRequestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final SalvarUsuarioUseCase salvarUsuarioUseCase;
    private final DetalhesUsuarioUseCase detalhesUsuarioUseCase;
    private final AtualizarUsuarioUseCase atualizarUsuarioUseCase;
    private final GerenciarEnderecoUseCase gerenciarEnderecoUseCase;
    private final ListarAnunciosPorVendedorUseCase listarAnunciosPorVendedorUseCase;
    private final ConsultarAtividadeUseCase consultarAtividadeUseCase;

    public record AtividadeResponse(String tipo, String descricao, String ip, String navegador, java.time.LocalDateTime quando) {
    }

    /** "Atividade recente": logins, saídas, troca de senha... da própria conta (auditoria) */
    @GetMapping("/me/atividade")
    public List<AtividadeResponse> atividade() {
        return consultarAtividadeUseCase.recentes(UsuarioAutenticadoUtil.obterUsuarioLogadoId()).stream()
                .map(e -> new AtividadeResponse(e.tipo().name(), e.tipo().descricao(), e.ip(), e.userAgent(), e.criadoEm()))
                .toList();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> salvarUsuario(@RequestBody @Valid UsuarioRequest request) {
        Usuario usuarioSalvo = salvarUsuarioUseCase.salvarUsuario(UsuarioRequestMapper.toCommand(request));
        return ResponseEntity
                .created(URI.create("/usuarios/" + usuarioSalvo.getId()))
                .body(UsuarioResponse.from(usuarioSalvo));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> buscarUsuarioLogado() {
        Usuario usuario = detalhesUsuarioUseCase.detalhesUsuario(UsuarioAutenticadoUtil.obterUsuarioLogadoId());
        return ResponseEntity.ok(UsuarioResponse.from(usuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarUsuario(@PathVariable UUID id) {
        return ResponseEntity.ok(UsuarioResponse.from(detalhesUsuarioUseCase.detalhesUsuario(id)));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> atualizarUsuario(@RequestBody @Valid AtualizarUsuarioRequest request) {
        Usuario usuario = atualizarUsuarioUseCase.atualizarUsuario(
                UsuarioRequestMapper.toCommand(UsuarioAutenticadoUtil.obterUsuarioLogadoId(), request));
        return ResponseEntity.ok(UsuarioResponse.from(usuario));
    }

    @PostMapping("/me/enderecos")
    public ResponseEntity<UsuarioResponse> adicionarEndereco(@RequestBody @Valid EnderecoRequest request) {
        Usuario usuario = gerenciarEnderecoUseCase.adicionarEndereco(
                UsuarioAutenticadoUtil.obterUsuarioLogadoId(), UsuarioRequestMapper.toCommand(request));
        return ResponseEntity.ok(UsuarioResponse.from(usuario));
    }

    @DeleteMapping("/me/enderecos/{enderecoId}")
    public ResponseEntity<UsuarioResponse> removerEndereco(@PathVariable UUID enderecoId) {
        Usuario usuario = gerenciarEnderecoUseCase.removerEndereco(UsuarioAutenticadoUtil.obterUsuarioLogadoId(), enderecoId);
        return ResponseEntity.ok(UsuarioResponse.from(usuario));
    }

    @GetMapping("/{id}/anuncios")
    public ResponseEntity<List<AnuncioResponse>> listarAnunciosDoUsuario(@PathVariable UUID id) {
        return ResponseEntity.ok(listarAnunciosPorVendedorUseCase.listarPorVendedor(id, UsuarioAutenticadoUtil.obterUsuarioLogadoId()).stream()
                .map(AnuncioResponse::from)
                .toList());
    }
}
