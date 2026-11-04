package com.psnf.api.psnf.application.arquivo;

import com.psnf.api.psnf.domain.arquivo.exception.ArquivoNaoEncontradoException;
import com.psnf.api.psnf.domain.arquivo.model.Arquivo;
import com.psnf.api.psnf.domain.arquivo.model.ConteudoArquivo;
import com.psnf.api.psnf.domain.arquivo.port.in.BaixarArquivoUseCase;
import com.psnf.api.psnf.domain.arquivo.port.out.ArmazenamentoArquivoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BaixarArquivoService implements BaixarArquivoUseCase {

    private final ArmazenamentoArquivoPort armazenamentoArquivoPort;

    @Override
    public ConteudoArquivo baixar(String nome) {
        if (!Arquivo.nomeValido(nome)) {
            throw new ArquivoNaoEncontradoException(nome);
        }
        return armazenamentoArquivoPort.carregar(nome)
                .orElseThrow(() -> new ArquivoNaoEncontradoException(nome));
    }
}
