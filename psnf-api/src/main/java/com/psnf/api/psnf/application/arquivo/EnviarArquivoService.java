package com.psnf.api.psnf.application.arquivo;

import com.psnf.api.psnf.domain.arquivo.exception.ArquivoInvalidoException;
import com.psnf.api.psnf.domain.arquivo.model.Arquivo;
import com.psnf.api.psnf.domain.arquivo.model.TipoArquivo;
import com.psnf.api.psnf.domain.arquivo.port.in.EnviarArquivoUseCase;
import com.psnf.api.psnf.domain.arquivo.port.in.command.EnviarArquivoCommand;
import com.psnf.api.psnf.domain.arquivo.port.out.ArmazenamentoArquivoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnviarArquivoService implements EnviarArquivoUseCase {

    private final ArmazenamentoArquivoPort armazenamentoArquivoPort;

    @Override
    public Arquivo enviar(EnviarArquivoCommand command) {
        TipoArquivo tipo = TipoArquivo.detectar(command.conteudo())
                .orElseThrow(() -> new ArquivoInvalidoException(
                        "Tipo de arquivo não suportado. Envie uma imagem (JPG, PNG, WEBP, GIF), PDF ou XML"));

        long tamanho = command.conteudo().length;
        if (tamanho > tipo.tamanhoMaximo()) {
            throw new ArquivoInvalidoException("Arquivo muito grande: o limite para " + tipo.getExtensao().toUpperCase()
                    + " é " + (tipo.tamanhoMaximo() / (1024 * 1024)) + " MB");
        }

        Arquivo arquivo = Arquivo.novo(command.nomeOriginal(), tipo, tamanho);
        armazenamentoArquivoPort.salvar(arquivo.nome(), tipo.getContentType(), command.conteudo());
        return arquivo;
    }
}
