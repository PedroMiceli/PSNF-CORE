package com.psnf.api.psnf.infrastructure.mock;

import com.psnf.api.psnf.infrastructure.persistence.usuario.repository.UsuarioJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * SOMENTE PARA DESENVOLVIMENTO: lista os usuários para o seletor "Logado como" do front, que simula o
 * login enquanto não existe autenticação. Fica ativo junto com os usuários mock
 * (psnf.mock.usuarios.enabled) e deve ser removido junto com eles quando o login real existir.
 */
@RestController
@RequestMapping("/dev/usuarios")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "psnf.mock.usuarios.enabled", havingValue = "true", matchIfMissing = true)
public class UsuariosDevController {

    private static final int LIMITE = 200;

    public record UsuarioDev(UUID id, String nome, String email) {
    }

    private final UsuarioJPARepository usuarioJPARepository;

    @GetMapping
    public List<UsuarioDev> listar() {
        return usuarioJPARepository.findAll(PageRequest.of(0, LIMITE, Sort.by("nome"))).stream()
                .map(u -> new UsuarioDev(u.getId(), u.getNome(), u.getEmail()))
                .toList();
    }
}
