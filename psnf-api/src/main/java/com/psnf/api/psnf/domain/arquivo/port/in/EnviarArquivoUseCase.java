package com.psnf.api.psnf.domain.arquivo.port.in;

import com.psnf.api.psnf.domain.arquivo.model.Arquivo;
import com.psnf.api.psnf.domain.arquivo.port.in.command.EnviarArquivoCommand;

public interface EnviarArquivoUseCase {

    Arquivo enviar(EnviarArquivoCommand command);
}
