package com.psnf.api.psnf.domain.usuario;

import com.psnf.api.psnf.application.usuario.VerificacaoCnpj;
import com.psnf.api.psnf.domain.usuario.exception.CnpjInativoException;
import com.psnf.api.psnf.domain.usuario.exception.CnpjJaCadastradoException;
import com.psnf.api.psnf.domain.usuario.exception.ConsultaCnpjIndisponivelException;
import com.psnf.api.psnf.domain.usuario.model.EmpresaConsultada;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.out.ConsultaCnpjPort;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificacaoCnpjTest {

    private static final String ATIVO = "19131243000197", BAIXADO = "00000001099914", SEM_BASE = "00000002099969";
    private int consultas = 0;
    private boolean foraDoAr = false;
    private final Set<String> emUso = new HashSet<>();

    private final ConsultaCnpjPort port = cnpj -> {
        consultas++;
        if (foraDoAr) throw new ConsultaCnpjIndisponivelException("fora", null);
        return switch (cnpj) {
            case ATIVO -> Optional.of(empresa(cnpj, "ATIVA"));
            case BAIXADO -> Optional.of(empresa(cnpj, "BAIXADA"));
            default -> Optional.empty();
        };
    };
    private final UsuarioRepositoryPort usuarios = new UsuarioRepositoryPort() {
        public Usuario save(Usuario u) { return u; }
        public Usuario buscarPorId(UUID id) { return null; }
        public Map<UUID, Usuario> buscarPorIds(Collection<UUID> ids) { return Map.of(); }
        public boolean existePorEmail(String e) { return false; }
        public Optional<Usuario> buscarPorEmail(String e) { return Optional.empty(); }
        public boolean cnpjEmUsoPorOutro(String c, UUID u) { return emUso.contains(c); }
    };
    private final VerificacaoCnpj verificacao = new VerificacaoCnpj(port, usuarios);

    private static EmpresaConsultada empresa(String cnpj, String situacao) {
        return new EmpresaConsultada(cnpj, "OPEN KNOWLEDGE BRASIL", null, situacao, null, null, null, null, null, "SAO PAULO", "SP");
    }

    private static Usuario usuario() {
        return Usuario.restaurar(UUID.randomUUID(), LocalDateTime.now(), null, null, "Empresa", "e@x.com", "h", null, null, null, List.of());
    }

    @Test
    void cnpjAtivoNaReceitaFicaVerificadoComARazaoSocial() {
        Usuario u = usuario();
        verificacao.aplicar(u, "19.131.243/0001-97");
        assertThat(u.cnpjVerificado()).isTrue();
        assertThat(u.getRazaoSocial()).isEqualTo("OPEN KNOWLEDGE BRASIL");
    }

    @Test
    void cnpjBaixadoERecusado() {
        assertThatThrownBy(() -> verificacao.aplicar(usuario(), BAIXADO))
                .isInstanceOf(CnpjInativoException.class).hasMessageContaining("BAIXADA");
    }

    @Test
    void foraDaBaseOuConsultaForaDoArSalvaSemVerificar() {
        Usuario u = usuario();
        verificacao.aplicar(u, SEM_BASE);
        assertThat(u.getCnpj()).isEqualTo(SEM_BASE);
        assertThat(u.cnpjVerificado()).isFalse();

        foraDoAr = true;
        Usuario v = usuario();
        verificacao.aplicar(v, ATIVO);
        assertThat(v.getCnpj()).isEqualTo(ATIVO);
        assertThat(v.cnpjVerificado()).isFalse();
    }

    @Test
    void mesmoCnpjJaVerificadoNaoConsultaDeNovoEVazioRemove() {
        Usuario u = usuario();
        verificacao.aplicar(u, ATIVO);
        verificacao.aplicar(u, ATIVO);
        assertThat(consultas).isEqualTo(1);
        verificacao.aplicar(u, null); // ausente: mantém
        assertThat(u.getCnpj()).isEqualTo(ATIVO);
        verificacao.aplicar(u, "");
        assertThat(u.getCnpj()).isNull();
        assertThat(u.getRazaoSocial()).isNull();
    }

    @Test
    void cnpjDeOutraContaERecusadoSemConsultar() {
        emUso.add(ATIVO);
        assertThatThrownBy(() -> verificacao.aplicar(usuario(), ATIVO)).isInstanceOf(CnpjJaCadastradoException.class);
        assertThat(consultas).isZero();
    }
}
