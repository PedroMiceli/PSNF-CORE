package com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido;

import com.psnf.api.psnf.application.ordemDePedido.SalvarOrdemDePedidoService;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.command.CadastrarUsuarioCommand;
import com.psnf.api.psnf.infrastructure.web.controller.ordemDePedido.dto.request.OrdemDePedidoRequest;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.request.UsuarioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.dto.response.UsuarioResponse;
import com.psnf.api.psnf.infrastructure.web.controller.usuario.mapper.UsuarioRequestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ordemDePedido")
@RequiredArgsConstructor
public class OrdemDePedidoController {

    private final SalvarOrdemDePedidoService salvarOrdemDePedidoService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> salvarOrdemDePedido(@RequestBody @Valid OrdemDePedidoRequest request){
        CadastrarUsuarioCommand commmand = UsuarioRequestMapper.toCommand(request);
        Usuario usuarioSalvo = salvarOrdemDePedidoService.cadastrarOrdemDePedido(commmand);
        return ResponseEntity.ok(UsuarioResponse.from(usuarioSalvo));
    }
}
