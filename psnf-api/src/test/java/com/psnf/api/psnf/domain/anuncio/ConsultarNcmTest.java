package com.psnf.api.psnf.domain.anuncio;

import com.psnf.api.psnf.application.anuncio.ConsultarNcmService;
import com.psnf.api.psnf.domain.anuncio.exception.NcmNaoEncontradoException;
import com.psnf.api.psnf.domain.anuncio.model.Ncm;
import com.psnf.api.psnf.domain.anuncio.port.out.ConsultaNcmPort;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConsultarNcmTest {

    final List<String> buscas = new ArrayList<>();
    final ConsultarNcmService service = new ConsultarNcmService(new ConsultaNcmPort() {
        @Override
        public List<Ncm> pesquisar(String termo) {
            buscas.add(termo);
            return List.of(new Ncm("73.18", "Parafusos..."), new Ncm("7318.1", "Roscados"),
                    new Ncm("7318.15.00", "Outros parafusos"), new Ncm("7318.16.00", "Porcas"));
        }

        @Override
        public Optional<Ncm> consultar(String codigo) {
            return codigo.equals("73181500") ? Optional.of(new Ncm("7318.15.00", "Outros parafusos")) : Optional.empty();
        }
    });

    @Test
    void pesquisaDevolveSoCodigosCompletos() {
        assertEquals(List.of("7318.15.00", "7318.16.00"), service.pesquisar("parafuso").stream().map(Ncm::codigo).toList());
    }

    @Test
    void buscaNumericaIgnoraOsPontos() {
        service.pesquisar("7318.15");
        assertEquals(List.of("731815"), buscas);
    }

    @Test
    void termoCurtoNemChegaAoServicoExterno() {
        assertThrows(IllegalArgumentException.class, () -> service.pesquisar("pa"));
        assertThrows(IllegalArgumentException.class, () -> service.pesquisar(null));
        assertTrue(buscas.isEmpty());
    }

    @Test
    void consultaPorCodigo() {
        assertEquals("Outros parafusos", service.consultar("7318.15.00").descricao());
        assertThrows(NcmNaoEncontradoException.class, () -> service.consultar("99999999"));
        assertThrows(IllegalArgumentException.class, () -> service.consultar("7318"));
    }
}
