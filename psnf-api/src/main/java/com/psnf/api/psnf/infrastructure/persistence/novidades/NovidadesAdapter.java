package com.psnf.api.psnf.infrastructure.persistence.novidades;

import com.psnf.api.psnf.domain.novidades.model.Novidades;
import com.psnf.api.psnf.domain.novidades.port.out.NovidadesPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Três consultas agregadas (count/max) que o PostgreSQL resolve pelos índices de comprador/vendedor
 * (db/indices.sql). Nenhuma entidade é carregada: cada uma devolve uma única linha.
 */
@Component
public class NovidadesAdapter implements NovidadesPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Novidades.Versao negociacoes(UUID usuarioId) {
        Object[] r = (Object[]) em.createQuery("""
                        select count(n), max(coalesce(n.ultimaMensagemEm, n.dataCadastro))
                        from NegociacaoJpa n where n.comprador.id = :u or n.vendedor.id = :u""")
                .setParameter("u", usuarioId).getSingleResult();
        return new Novidades.Versao(((Number) r[0]).longValue(), (LocalDateTime) r[1]);
    }

    @Override
    public Novidades.Versao ordens(UUID usuarioId) {
        Object[] r = (Object[]) em.createQuery("""
                        select count(o), max(coalesce(o.atualizadaEm, o.dataCadastro))
                        from OrdemDePedidoJpa o where o.comprador.id = :u or o.vendedor.id = :u""")
                .setParameter("u", usuarioId).getSingleResult();
        return new Novidades.Versao(((Number) r[0]).longValue(), (LocalDateTime) r[1]);
    }

    @Override
    public Novidades.Incidentes incidentes(UUID usuarioId) {
        // mensagens escritas pelo outro lado depois da última vez que este usuário viu os incidentes da ordem
        Object[] r = (Object[]) em.createQuery("""
                        select count(m), count(distinct o.id), max(m.dataCadastro)
                        from OrdemDePedidoJpa o join o.mensagensIncidente m
                        where m.usuarioCriador.id <> :u
                          and ((o.comprador.id = :u and (o.incidentesLidosCompradorEm is null or m.dataCadastro > o.incidentesLidosCompradorEm))
                            or (o.vendedor.id = :u and (o.incidentesLidosVendedorEm is null or m.dataCadastro > o.incidentesLidosVendedorEm)))""")
                .setParameter("u", usuarioId).getSingleResult();
        return new Novidades.Incidentes(((Number) r[0]).longValue(), ((Number) r[1]).longValue(), (LocalDateTime) r[2]);
    }
}
