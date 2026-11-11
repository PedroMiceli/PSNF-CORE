package com.psnf.api.psnf.infrastructure.mock;

import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Usuários de teste para desenvolvimento, criados no banco pelo {@link UsuarioMockInitializer}.
 * Entre com vendedor@mock.psnf ou comprador@mock.psnf, senha {@value #SENHA}.
 * <p>
 * Senha conhecida: desligue em produção com {@code PSNF_USUARIOS_TESTE=false}.
 */
public final class UsuarioMock {

    public static final UUID VENDEDOR_ID = UUID.fromString("3c288400-f58f-4d28-905c-f7dba388d6c9");
    public static final UUID COMPRADOR_ID = UUID.fromString("7f1e2a5b-9c3d-4e8f-a6b7-1d2c3e4f5a6b");

    public static final String SENHA = "123456";
    /** CNPJs de teste (dígitos válidos, empresas fictícias) */
    public static final String VENDEDOR_CNPJ = "11222333000181";
    public static final String COMPRADOR_CNPJ = "44555666000181";

    public static final Usuario VENDEDOR = Usuario.restaurar(
            VENDEDOR_ID, LocalDateTime.now(), null, null,
            "Vendedor Mock", "vendedor@mock.psnf", null, null, null, "11999990001", List.of());

    public static final Usuario COMPRADOR = Usuario.restaurar(
            COMPRADOR_ID, LocalDateTime.now(), null, null,
            "Comprador Mock", "comprador@mock.psnf", null, null, null, "11999990002", List.of());

    public static final List<Usuario> TODOS = List.of(VENDEDOR, COMPRADOR);

    private UsuarioMock() {
    }
}
