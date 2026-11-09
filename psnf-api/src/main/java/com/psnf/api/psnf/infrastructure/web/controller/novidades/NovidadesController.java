package com.psnf.api.psnf.infrastructure.web.controller.novidades;

import com.psnf.api.psnf.domain.novidades.model.Novidades;
import com.psnf.api.psnf.domain.novidades.port.in.ConsultarNovidadesUseCase;
import com.psnf.api.psnf.infrastructure.web.controller.UsuarioAutenticadoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * "Mudou alguma coisa para mim?" — a única consulta periódica do site (fora o chat aberto).
 * Ex.: GET /me/novidades
 * {
 *   "negociacoes": { "versao": "16-2026-11-09T15:16:01", "total": 16 },
 *   "ordens":      { "versao": "50-2026-11-09T15:20:44", "total": 50 },
 *   "incidentes":  { "versao": "1-2026-11-09T14:16:30", "naoLidas": 1, "ordens": 1 }
 * }
 * O front guarda as versões e só busca as listas/telas completas quando uma delas muda.
 */
@RestController
@RequestMapping("/me/novidades")
@RequiredArgsConstructor
public class NovidadesController {

    private final ConsultarNovidadesUseCase consultarNovidadesUseCase;

    public record VersaoResponse(String versao, long total) {
    }

    public record IncidentesResponse(String versao, long naoLidas, long ordens) {
    }

    public record NovidadesResponse(VersaoResponse negociacoes, VersaoResponse ordens, IncidentesResponse incidentes) {
        static NovidadesResponse from(Novidades n) {
            return new NovidadesResponse(
                    new VersaoResponse(n.negociacoes().chave(), n.negociacoes().total()),
                    new VersaoResponse(n.ordens().chave(), n.ordens().total()),
                    new IncidentesResponse(n.incidentes().chave(), n.incidentes().naoLidas(), n.incidentes().ordens()));
        }
    }

    @GetMapping
    public ResponseEntity<NovidadesResponse> consultar() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore()) // é pessoal e muda a todo momento
                .body(NovidadesResponse.from(consultarNovidadesUseCase.consultar(UsuarioAutenticadoUtil.obterUsuarioLogadoId())));
    }
}
