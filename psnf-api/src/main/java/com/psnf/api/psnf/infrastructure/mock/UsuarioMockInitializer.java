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
 * Usa {@code ON CONFLICT DO NOTHING}: se já existirem, nada é alterado.
 * <p>
 * Desligue com {@code PSNF_USUARIOS_TESTE=false} (psnf.mock.usuarios.enabled): em produção, contas com
 * senha conhecida seriam uma porta aberta.
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
            String cnpj = usuario.getId().equals(UsuarioMock.VENDEDOR_ID) ? UsuarioMock.VENDEDOR_CNPJ : UsuarioMock.COMPRADOR_CNPJ;
            int inseridos = jdbcTemplate.update("""
                            INSERT INTO usuario (id, nome, email, senha, telefone, data_cadastro, email_confirmado_em, cnpj)
                            VALUES (?, ?, ?, ?, ?, ?, now(), ?)
                            ON CONFLICT DO NOTHING
                            """,
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    senhaEncoderPort.criptografar(UsuarioMock.SENHA),
                    usuario.getTelefone(),
                    LocalDateTime.now(),
                    cnpj
            );
            if (inseridos > 0) {
                logger.info("Usuário mock criado: {} ({})", usuario.getNome(), usuario.getId());
            } else {
                // bancos de antes do CNPJ: os usuários de teste ganham o CNPJ de teste (se ninguém o usa)
                jdbcTemplate.update("UPDATE usuario SET cnpj = ? WHERE id = ? AND cnpj IS NULL "
                        + "AND NOT EXISTS (SELECT 1 FROM usuario o WHERE o.cnpj = ?)", cnpj, usuario.getId(), cnpj);
            }
        }
        logger.warn("Usuários de teste ativos (senha {}): {} e {}. Desligue em produção com PSNF_USUARIOS_TESTE=false",
                UsuarioMock.SENHA, UsuarioMock.VENDEDOR.getEmail(), UsuarioMock.COMPRADOR.getEmail());
    }
}
