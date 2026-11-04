package com.psnf.api.psnf.domain.arquivo;

import com.psnf.api.psnf.application.arquivo.BaixarArquivoService;
import com.psnf.api.psnf.application.arquivo.EnviarArquivoService;
import com.psnf.api.psnf.domain.arquivo.exception.ArquivoInvalidoException;
import com.psnf.api.psnf.domain.arquivo.exception.ArquivoNaoEncontradoException;
import com.psnf.api.psnf.domain.arquivo.model.Arquivo;
import com.psnf.api.psnf.domain.arquivo.model.ConteudoArquivo;
import com.psnf.api.psnf.domain.arquivo.model.TipoArquivo;
import com.psnf.api.psnf.domain.arquivo.port.in.command.EnviarArquivoCommand;
import com.psnf.api.psnf.domain.arquivo.port.out.ArmazenamentoArquivoPort;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EnviarArquivoTest {

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13};
    static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10};
    static final byte[] PDF = "%PDF-1.7\n...".getBytes(StandardCharsets.US_ASCII);
    static final byte[] WEBP = "RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.US_ASCII);
    static final byte[] XML = "﻿  <?xml version=\"1.0\"?><nfeProc/>".getBytes(StandardCharsets.UTF_8);

    /** Storage em memória para testar sem MinIO. */
    static class StorageFake implements ArmazenamentoArquivoPort {
        final Map<String, byte[]> objetos = new HashMap<>();
        final Map<String, String> tipos = new HashMap<>();

        public void salvar(String nome, String contentType, byte[] conteudo) {
            objetos.put(nome, conteudo);
            tipos.put(nome, contentType);
        }

        public Optional<ConteudoArquivo> carregar(String nome) {
            return Optional.ofNullable(objetos.get(nome))
                    .map(c -> new ConteudoArquivo(nome, tipos.get(nome), c.length, new ByteArrayInputStream(c)));
        }
    }

    final StorageFake storage = new StorageFake();
    final EnviarArquivoService enviar = new EnviarArquivoService(storage);
    final BaixarArquivoService baixar = new BaixarArquivoService(storage);

    @Test
    void detectaTipoPeloConteudo() {
        assertEquals(TipoArquivo.PNG, TipoArquivo.detectar(PNG).orElseThrow());
        assertEquals(TipoArquivo.JPEG, TipoArquivo.detectar(JPEG).orElseThrow());
        assertEquals(TipoArquivo.PDF, TipoArquivo.detectar(PDF).orElseThrow());
        assertEquals(TipoArquivo.WEBP, TipoArquivo.detectar(WEBP).orElseThrow());
        assertEquals(TipoArquivo.XML, TipoArquivo.detectar(XML).orElseThrow());
        assertTrue(TipoArquivo.detectar("<html><script>".getBytes()).isEmpty());
    }

    @Test
    void salvaComNomeGeradoEContentTypeDetectado() {
        // nome "malicioso" e extensão errada: são ignorados
        Arquivo a = enviar.enviar(new EnviarArquivoCommand("../../etc/passwd.exe", PNG));

        assertTrue(a.nome().endsWith(".png"));
        assertEquals("/arquivos/" + a.nome(), a.caminho());
        assertTrue(Arquivo.nomeValido(a.nome()));
        assertEquals("image/png", storage.tipos.get(a.nome()));

        ConteudoArquivo lido = baixar.baixar(a.nome());
        assertEquals("image/png", lido.contentType());
        assertEquals(PNG.length, lido.tamanho());
    }

    @Test
    void rejeitaTipoNaoSuportadoEArquivoVazio() {
        assertThrows(ArquivoInvalidoException.class,
                () -> enviar.enviar(new EnviarArquivoCommand("x.html", "<html>".getBytes())));
        assertThrows(ArquivoInvalidoException.class, () -> new EnviarArquivoCommand("x.png", new byte[0]));
        assertTrue(storage.objetos.isEmpty());
    }

    @Test
    void respeitaLimiteDeTamanhoPorTipo() {
        byte[] imagemGrande = new byte[(int) TipoArquivo.TAMANHO_MAXIMO_IMAGEM + 1];
        System.arraycopy(PNG, 0, imagemGrande, 0, PNG.length);
        assertThrows(ArquivoInvalidoException.class, () -> enviar.enviar(new EnviarArquivoCommand("g.png", imagemGrande)));

        byte[] pdfGrande = new byte[(int) TipoArquivo.TAMANHO_MAXIMO_IMAGEM + 1]; // 5 MB+1: ok para PDF
        System.arraycopy(PDF, 0, pdfGrande, 0, PDF.length);
        assertDoesNotThrow(() -> enviar.enviar(new EnviarArquivoCommand("nf.pdf", pdfGrande)));
    }

    @Test
    void baixarValidaNome() {
        assertFalse(Arquivo.nomeValido("../segredo.png"));
        assertFalse(Arquivo.nomeValido("1-1-1-1-1.png"));
        assertFalse(Arquivo.nomeValido("3f2a1c9e-1111-2222-3333-444455556666.exe"));
        assertThrows(ArquivoNaoEncontradoException.class, () -> baixar.baixar("../segredo.png"));
        assertThrows(ArquivoNaoEncontradoException.class, () -> baixar.baixar("3f2a1c9e-1111-2222-3333-444455556666.png"));
    }
}
