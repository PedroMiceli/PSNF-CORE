package com.psnf.api.psnf.infrastructure.persistence.auditoria;

import com.psnf.api.psnf.domain.auditoria.model.EventoAuditoria;
import com.psnf.api.psnf.domain.auditoria.model.TipoEventoAuditoria;
import com.psnf.api.psnf.domain.auditoria.port.out.AuditoriaPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class AuditoriaAdapter implements AuditoriaPort {

    private static final Logger logger = LoggerFactory.getLogger("auditoria");
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private final EventoAuditoriaJpaRepository repo;

    /**
     * Grava em segundo plano (virtual thread), fora da transação de quem chamou:
     * <ul>
     *   <li>fica registrado mesmo quando a operação é desfeita (ex.: login com senha errada);</li>
     *   <li>a requisição não segura duas conexões do pool ao mesmo tempo. Com REQUIRES_NEW, 30 logins
     *       simultâneos ocupariam as 30 conexões e todos esperariam a segunda: o pool travaria.</li>
     * </ul>
     * IP e navegador são lidos agora, enquanto a requisição existe.
     */
    @Override
    public void registrar(UUID usuarioId, TipoEventoAuditoria tipo, String detalhe) {
        EventoAuditoriaJpa e = new EventoAuditoriaJpa();
        e.setId(UUID.randomUUID());
        e.setUsuarioId(usuarioId);
        e.setTipo(tipo);
        e.setDetalhe(cortar(detalhe, 500));
        e.setCriadoEm(LocalDateTime.now());
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest req = attrs.getRequest();
            e.setIp(cortar(req.getRemoteAddr(), 64));
            e.setUserAgent(cortar(req.getHeader(HttpHeaders.USER_AGENT), 255));
        }
        EXECUTOR.submit(() -> {
            try {
                repo.save(e); // o save do Spring Data abre a própria transação
            } catch (RuntimeException ex) {
                logger.error("Falha ao gravar auditoria {} (usuário {})", tipo, usuarioId, ex);
            }
        });
        // também no log, para ferramentas de monitoramento (alertas de muitas falhas, por exemplo)
        logger.info("{} usuario={} ip={} {}", tipo, usuarioId, e.getIp(), detalhe == null ? "" : detalhe);
    }

    @Override
    public List<EventoAuditoria> recentesDoUsuario(UUID usuarioId, int limite) {
        return repo.findByUsuarioIdOrderByCriadoEmDesc(usuarioId, PageRequest.of(0, limite)).stream()
                .map(e -> new EventoAuditoria(e.getId(), e.getUsuarioId(), e.getTipo(), e.getDetalhe(), e.getIp(),
                        e.getUserAgent(), e.getCriadoEm()))
                .toList();
    }

    @Override
    public int apagarAnterioresA(LocalDateTime limite) { return repo.apagarAnterioresA(limite); }

    private static String cortar(String s, int max) { return s == null || s.length() <= max ? s : s.substring(0, max); }
}
