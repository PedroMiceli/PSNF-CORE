package com.psnf.api.psnf.domain.usuario;

import com.psnf.api.psnf.domain.usuario.model.Endereco;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnderecoTest {

    private Endereco criar(String uf) {
        return Endereco.criar("Rua A", "10", null, "Centro", "São Paulo", "01000-000", uf);
    }

    @Test
    void normalizaUfParaMaiusculas() {
        assertEquals("SP", criar("sp").getUf());
        assertEquals("RJ", criar(" rj ").getUf());
    }

    @Test
    void aceitaAs27Ufs() {
        assertEquals(27, Endereco.UFS.size());
        Endereco.UFS.forEach(uf -> assertDoesNotThrow(() -> criar(uf)));
    }

    @Test
    void rejeitaUfInvalida() {
        assertThrows(IllegalArgumentException.class, () -> criar("XX"));
        assertThrows(IllegalArgumentException.class, () -> criar("São Paulo"));
        assertThrows(IllegalArgumentException.class, () -> criar(""));
        assertThrows(IllegalArgumentException.class, () -> criar(null));
    }
}
