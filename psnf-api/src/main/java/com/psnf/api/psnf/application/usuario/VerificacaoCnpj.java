package com.psnf.api.psnf.application.usuario;

import com.psnf.api.psnf.domain.usuario.exception.CnpjInativoException;
import com.psnf.api.psnf.domain.usuario.exception.CnpjJaCadastradoException;
import com.psnf.api.psnf.domain.usuario.exception.CnpjNaoEncontradoException;
import com.psnf.api.psnf.domain.usuario.exception.ConsultaCnpjIndisponivelException;
import com.psnf.api.psnf.domain.usuario.model.Cnpj;
import com.psnf.api.psnf.domain.usuario.model.EmpresaConsultada;
import com.psnf.api.psnf.domain.usuario.model.Usuario;
import com.psnf.api.psnf.domain.usuario.port.in.ConsultarCnpjUseCase;
import com.psnf.api.psnf.domain.usuario.port.out.ConsultaCnpjPort;
import com.psnf.api.psnf.domain.usuario.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Regras do CNPJ ao cadastrar/alterar a conta, e a consulta avulsa usada pelo front.
 * <ul>
 *   <li>ATIVA na Receita: guarda a razão social e marca como verificado;</li>
 *   <li>baixada, inapta, suspensa, nula: recusa;</li>
 *   <li>não encontrado na base pública (ex.: alfanumérico recém-emitido) ou BrasilAPI fora do ar: aceita,
 *       sem verificação. Uma instabilidade externa não pode impedir ninguém de se cadastrar.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class VerificacaoCnpj implements ConsultarCnpjUseCase {

    private static final Logger logger = LoggerFactory.getLogger(VerificacaoCnpj.class);

    private final ConsultaCnpjPort consulta;
    private final UsuarioRepositoryPort usuarios;

    @Override
    public EmpresaConsultada consultar(String cnpj) {
        String c = Cnpj.normalizar(cnpj);
        if (c == null) throw new IllegalArgumentException("Informe o CNPJ");
        return consulta.consultar(c).orElseThrow(CnpjNaoEncontradoException::new);
    }

    /** aplica o CNPJ informado ao usuário (null = não mexe; "" = remove) */
    public void aplicar(Usuario usuario, String informado) {
        if (informado == null) return;
        String anterior = usuario.getCnpj();
        usuario.definirCnpj(informado);
        String novo = usuario.getCnpj();
        if (novo == null) {
            usuario.registrarVerificacaoCnpj(null, null);
            return;
        }
        if (usuarios.cnpjEmUsoPorOutro(novo, usuario.getId())) throw new CnpjJaCadastradoException();
        if (Objects.equals(novo, anterior) && usuario.cnpjVerificado()) return; // mesmo CNPJ, já verificado

        Optional<EmpresaConsultada> empresa;
        try {
            empresa = consulta.consultar(novo);
        } catch (ConsultaCnpjIndisponivelException e) {
            logger.warn("CNPJ {} salvo sem verificação: consulta indisponível", novo);
            usuario.registrarVerificacaoCnpj(null, null);
            return;
        }
        if (empresa.isPresent() && !empresa.get().ativa()) throw new CnpjInativoException(empresa.get().situacao());
        usuario.registrarVerificacaoCnpj(empresa.map(EmpresaConsultada::razaoSocial).orElse(null),
                empresa.isPresent() ? LocalDateTime.now() : null);
    }
}
