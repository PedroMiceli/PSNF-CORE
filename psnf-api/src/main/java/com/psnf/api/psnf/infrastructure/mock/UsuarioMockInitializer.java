package com.psnf.api.psnf.infrastructure.mock;

import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.SenhaEncoderPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Garante que os usuários de {@link UsuarioMock} existam também no banco.
 * <p>
 * Eles vivem em memória, mas ordem de pedido e anúncio têm chave estrangeira para a tabela
 * {@code usuario} — sem essa linha no banco o INSERT da ordem falharia.
 * Usa {@code ON CONFLICT DO NOTHING}: se já existirem, nada é alterado.
 * <p>
 * Desligue com {@code psnf.mock.usuarios.enabled=false}.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "psnf.mock.usuarios.enabled", havingValue = "true", matchIfMissing = true)
public class UsuarioMockInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioMockInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final SenhaEncoderPort senhaEncoderPort;

    @Override
    public void run(ApplicationArguments args) {
        for (Usuario usuario : UsuarioMock.TODOS) {
            int inseridos = jdbcTemplate.update("""
                            INSERT INTO usuario (id, nome, email, senha, telefone, data_cadastro)
                            VALUES (?, ?, ?, ?, ?, ?)
                            ON CONFLICT DO NOTHING
                            """,
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    senhaEncoderPort.criptografar(UsuarioMock.SENHA),
                    usuario.getTelefone(),
                    LocalDateTime.now()
            );
            if (inseridos > 0) {
                logger.info("Usuário mock criado: {} ({})", usuario.getNome(), usuario.getId());
            }
        }
        logger.info("Usuários mock -> VENDEDOR: {} | COMPRADOR: {}", UsuarioMock.VENDEDOR_ID, UsuarioMock.COMPRADOR_ID);
    }
}
