package com.psnf.api.psnf.infrastructure.mock;

import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Usuários fixos em memória, usados enquanto não existe autenticação.
 * <p>
 * - VENDEDOR: é o usuário "logado" padrão (cria anúncios e ordens de pedido).
 * - COMPRADOR: use o id dele como {@code compradorId} ao criar uma ordem, e no header
 *   {@code X-Usuario-Id} para agir como comprador (pagar, confirmar entrega...).
 * <p>
 * Para remover depois: apagar este pacote e trocar o fallback do UsuarioAutenticadoUtil.
 */
public final class UsuarioMock {

    public static final UUID VENDEDOR_ID = UUID.fromString("3c288400-f58f-4d28-905c-f7dba388d6c9");
    public static final UUID COMPRADOR_ID = UUID.fromString("7f1e2a5b-9c3d-4e8f-a6b7-1d2c3e4f5a6b");

    public static final String SENHA = "123456";

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
