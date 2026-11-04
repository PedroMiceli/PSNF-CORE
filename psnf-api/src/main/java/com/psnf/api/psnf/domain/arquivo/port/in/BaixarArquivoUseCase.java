package com.psnf.api.psnf.domain.arquivo.port.in;

import com.psnf.api.psnf.domain.arquivo.model.ConteudoArquivo;

public interface BaixarArquivoUseCase {

    ConteudoArquivo baixar(String nome);
}
