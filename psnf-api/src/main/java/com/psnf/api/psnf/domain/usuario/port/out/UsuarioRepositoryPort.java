package com.psnf.api.psnf.domain.usuario.port.out;

import com.psnf.api.psnf.domain.usuario.model.Usuario;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface UsuarioRepositoryPort {

    Usuario save(Usuario usuario);

    Usuario buscarPorId(UUID usuarioId);

    /** Vários de uma vez (uma consulta + endereços em lote). Ids inexistentes ficam de fora do mapa. */
    Map<UUID, Usuario> buscarPorIds(Collection<UUID> usuarioIds);

    boolean existePorEmail(String email);
}
