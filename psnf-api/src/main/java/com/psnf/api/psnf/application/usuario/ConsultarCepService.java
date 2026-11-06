package com.psnf.api.psnf.application.usuario;

import com.psnf.api.psnf.domain.usuario.exception.CepNaoEncontradoException;
import com.psnf.api.psnf.domain.usuario.model.EnderecoConsultado;
import com.psnf.api.psnf.domain.usuario.port.in.ConsultarCepUseCase;
import com.psnf.api.psnf.domain.usuario.port.out.ConsultaCepPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultarCepService implements ConsultarCepUseCase {

    private final ConsultaCepPort consultaCepPort;

    @Override
    public EnderecoConsultado consultar(String cep) {
        String digitos = cep == null ? "" : cep.replaceAll("\\D", "");
        if (digitos.length() != 8) {
            throw new IllegalArgumentException("CEP inválido: informe os 8 dígitos (ex.: 01310-100)");
        }
        return consultaCepPort.consultar(digitos)
                .orElseThrow(() -> new CepNaoEncontradoException(digitos.substring(0, 5) + "-" + digitos.substring(5)));
    }
}
