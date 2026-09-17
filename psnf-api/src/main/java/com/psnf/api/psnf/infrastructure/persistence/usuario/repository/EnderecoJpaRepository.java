package com.psnf.api.psnf.infrastructure.persistence.usuario.repository;

import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.EnderecoJpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnderecoJpaRepository extends JpaRepository<EnderecoJpa, Long> {
}
