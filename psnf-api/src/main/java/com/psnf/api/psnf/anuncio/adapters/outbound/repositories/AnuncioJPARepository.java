package com.psnf.api.psnf.anuncio.adapters.outbound.repositories;

import com.psnf.api.psnf.anuncio.adapters.outbound.entities.JPAAnuncio;
import com.psnf.api.psnf.anuncio.domain.models.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnuncioJPARepository extends JpaRepository<JPAAnuncio, UUID> {
}
