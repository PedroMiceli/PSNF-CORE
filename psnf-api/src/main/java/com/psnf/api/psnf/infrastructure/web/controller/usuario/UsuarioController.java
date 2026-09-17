package com.psnf.api.psnf.infrastructure.web.controller.usuario;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.DetalhesUsuarioUseCase;
import com.psnf.api.psnf.domain.usuario.port.in.SalvarUsuarioUseCase;
import com.psnf.api.psnf.domain.usuario.port.in.command.CadastrarUsuarioCommand;
import com.psnf.api.psnf.infrastructure.web.controller.UsuarioAutenticadoUtil;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request.UsuarioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.response.UsuarioResponse;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.mapper.UsuarioRequestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final SalvarUsuarioUseCase salvarUsuarioUseCase;
    private final DetalhesUsuarioUseCase detalhesUsuarioUseCase;

    @PostMapping
    public ResponseEntity<UsuarioResponse> salvarUsuario(@RequestBody @Valid UsuarioRequest request){
        CadastrarUsuarioCommand commmand = UsuarioRequestMapper.toCommand(request);
        Usuario usuarioSalvo = salvarUsuarioUseCase.salvarUsuario(commmand);
        return ResponseEntity.ok(UsuarioResponse.from(usuarioSalvo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarUsuario() {
        Usuario usuario = detalhesUsuarioUseCase.detalhesUsuario(UsuarioAutenticadoUtil.obterUsuarioLogadoId());
        return ResponseEntity.ok(UsuarioResponse.from(usuario));
    }

}
