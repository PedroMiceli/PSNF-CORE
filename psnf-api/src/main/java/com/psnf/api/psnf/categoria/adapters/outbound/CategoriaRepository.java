package com.psnf.api.psnf.categoria.adapters.outbound;

import com.psnf.api.psnf.categoria.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
}
