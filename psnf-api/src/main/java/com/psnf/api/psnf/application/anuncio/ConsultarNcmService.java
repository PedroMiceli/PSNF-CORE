package com.psnf.api.psnf.application.anuncio;

import com.psnf.api.psnf.domain.anuncio.exception.NcmNaoEncontradoException;
import com.psnf.api.psnf.domain.anuncio.model.Ncm;
import com.psnf.api.psnf.domain.anuncio.port.in.ConsultarNcmUseCase;
import com.psnf.api.psnf.domain.anuncio.port.out.ConsultaNcmPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultarNcmService implements ConsultarNcmUseCase {

    static final int MAXIMO_RESULTADOS = 20;

    private final ConsultaNcmPort consultaNcmPort;

    @Override
    public List<Ncm> pesquisar(String termo) {
        String t = termo == null ? "" : termo.trim();
        if (t.length() < 3) {
            throw new IllegalArgumentException("Informe ao menos 3 caracteres para buscar o NCM");
        }
        // quem digita números quer buscar pelo código: "7318.15" e "731815" dão o mesmo resultado
        String busca = t.matches("[\\d.\\s]+") ? t.replaceAll("\\D", "") : t;
        return consultaNcmPort.pesquisar(busca).stream()
                .filter(ncm -> ncm.codigo() != null && ncm.codigo().replaceAll("\\D", "").length() == 8)
                .limit(MAXIMO_RESULTADOS)
                .toList();
    }

    @Override
    public Ncm consultar(String codigo) {
        String digitos = codigo == null ? "" : codigo.replaceAll("\\D", "");
        if (digitos.length() != 8) {
            throw new IllegalArgumentException("NCM inválido: informe os 8 dígitos (ex.: 7318.15.00)");
        }
        return consultaNcmPort.consultar(digitos).orElseThrow(() -> new NcmNaoEncontradoException(
                digitos.substring(0, 4) + "." + digitos.substring(4, 6) + "." + digitos.substring(6)));
    }
}
