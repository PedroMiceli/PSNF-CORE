package com.psnf.api.psnf.domain.usuario;

import com.psnf.api.psnf.application.usuario.ConsultarCepService;
import com.psnf.api.psnf.domain.usuario.exception.CepNaoEncontradoException;
import com.psnf.api.psnf.domain.usuario.model.EnderecoConsultado;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConsultarCepTest {

    final List<String> consultados = new ArrayList<>();
    final ConsultarCepService service = new ConsultarCepService(cep -> {
        consultados.add(cep);
        return cep.equals("01310100")
                ? Optional.of(new EnderecoConsultado("01310-100", "Avenida Paulista", "Bela Vista", "São Paulo", "SP"))
                : Optional.empty();
    });

    @Test
    void aceitaCepComOuSemMascaraEConsultaSoOsDigitos() {
        assertEquals("Avenida Paulista", service.consultar("01310-100").logradouro());
        assertEquals("SP", service.consultar(" 01310100 ").uf());
        assertEquals(List.of("01310100", "01310100"), consultados);
    }

    @Test
    void cepComQuantidadeErradaDeDigitosNemChegaAoServicoExterno() {
        assertThrows(IllegalArgumentException.class, () -> service.consultar("1234"));
        assertThrows(IllegalArgumentException.class, () -> service.consultar("013101000"));
        assertThrows(IllegalArgumentException.class, () -> service.consultar(null));
        assertTrue(consultados.isEmpty());
    }

    @Test
    void cepInexistenteGera404() {
        CepNaoEncontradoException e = assertThrows(CepNaoEncontradoException.class, () -> service.consultar("99999999"));
        assertTrue(e.getMessage().contains("99999-999"));
    }
}
