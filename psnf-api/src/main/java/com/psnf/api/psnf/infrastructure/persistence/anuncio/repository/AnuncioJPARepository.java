package com.psnf.api.psnf.infrastructure.persistence.anuncio.repository;

import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnuncioJPARepository extends JpaRepository<AnuncioJpa, UUID> {
}
