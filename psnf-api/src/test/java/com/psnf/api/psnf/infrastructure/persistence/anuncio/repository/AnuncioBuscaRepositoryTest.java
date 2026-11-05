package com.psnf.api.psnf.infrastructure.persistence.anuncio.repository;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnuncioBuscaRepositoryTest {

    @Test
    void separaPalavrasIgnorandoSimbolosELetrasSoltas() {
        assertEquals(List.of("carrinho", "de", "metal"), AnuncioBuscaRepository.palavras("  Carrinho de METAL! "));
        assertEquals(List.of("água", "mineral"), AnuncioBuscaRepository.palavras("água-mineral"));
        assertEquals(List.of("balão", "nº", "9"), AnuncioBuscaRepository.palavras("balão nº 9"));
        assertTrue(AnuncioBuscaRepository.palavras("c++ & | ! :* (").isEmpty());
    }

    @Test
    void montaTsqueryComPrefixo() {
        assertEquals("carri:* & metal:*", AnuncioBuscaRepository.montarTsquery(List.of("carri", "metal")));
    }

    @Test
    void injecaoDeSintaxeNaoChegaAoTsquery() {
        String tsquery = AnuncioBuscaRepository.montarTsquery(AnuncioBuscaRepository.palavras("abc') | !(def <-> ghi:*"));
        assertEquals("abc:* & def:* & ghi:*", tsquery);
    }
}
