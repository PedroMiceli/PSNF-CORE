package com.psnf.api.psnf.usuario;

import com.psnf.api.psnf.domain.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<UUID, Usuario> {



}
