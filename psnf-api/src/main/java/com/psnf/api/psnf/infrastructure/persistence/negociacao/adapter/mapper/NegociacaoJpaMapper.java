package com.psnf.api.psnf.infrastructure.persistence.negociacao.adapter.mapper;

import com.psnf.api.psnf.domain.negociacao.model.MensagemNegociacao;
import com.psnf.api.psnf.domain.negociacao.model.Negociacao;
import com.psnf.api.psnf.infrastructure.persistence.anuncio.entity.jpa.AnuncioJpa;
import com.psnf.api.psnf.infrastructure.persistence.negociacao.entity.MensagemNegociacaoJpa;
import com.psnf.api.psnf.infrastructure.persistence.negociacao.entity.NegociacaoJpa;
import com.psnf.api.psnf.infrastructure.persistence.usuario.entity.UsuarioJpa;

import java.util.UUID;
import java.util.function.Function;

public class NegociacaoJpaMapper {

    private NegociacaoJpaMapper() {
    }

    /** Anúncio e usuários entram como referências (getReferenceById), sem carregá-los. */
    public static NegociacaoJpa toEntity(Negociacao n, Function<UUID, UsuarioJpa> usuarioRef, Function<UUID, AnuncioJpa> anuncioRef) {
        NegociacaoJpa jpa = new NegociacaoJpa();
        jpa.restaurarBase(n.getId(), n.getDataCadastro(), n.getDataAlteracao(), n.getDataExcluido());
        jpa.setAnuncio(anuncioRef.apply(n.getAnuncioId()));
        jpa.setComprador(usuarioRef.apply(n.getCompradorId()));
        jpa.setVendedor(usuarioRef.apply(n.getVendedorId()));
        jpa.setUltimaMensagemEm(n.getUltimaMensagemEm());
        n.getMensagens().forEach(m -> {
            MensagemNegociacaoJpa mj = new MensagemNegociacaoJpa();
            mj.restaurarBase(m.getId(), m.getDataCadastro(), m.getDataAlteracao(), m.getDataExcluido());
            mj.setTexto(m.getTexto());
            mj.setTipo(m.getTipo());
            mj.setAutor(usuarioRef.apply(m.getAutorId()));
            mj.setNegociacao(jpa);
            jpa.getMensagens().add(mj);
        });
        return jpa;
    }

    public static Negociacao toDomain(NegociacaoJpa jpa) {
        return Negociacao.restaurar(jpa.getId(), jpa.getDataCadastro(), jpa.getDataAlteracao(), jpa.getDataExcluido(),
                jpa.getAnuncio().getId(), jpa.getComprador().getId(), jpa.getVendedor().getId(),
                jpa.getMensagens().stream().map(m -> MensagemNegociacao.restaurar(m.getId(), m.getDataCadastro(),
                        m.getDataAlteracao(), m.getDataExcluido(), m.getAutor().getId(), m.getTexto(), m.getTipo())).toList(),
                jpa.getUltimaMensagemEm());
    }
}
