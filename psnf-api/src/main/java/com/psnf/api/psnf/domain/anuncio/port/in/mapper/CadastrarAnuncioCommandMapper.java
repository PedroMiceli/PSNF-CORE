package com.psnf.api.psnf.domain.anuncio.port.in.mapper;

import com.psnf.api.psnf.domain.anuncio.model.ImagemAnuncio;
import com.psnf.api.psnf.domain.anuncio.model.Valor;
import com.psnf.api.psnf.domain.anuncio.model.Variacao;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarImagemAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarValorAnuncioCommand;
import com.psnf.api.psnf.domain.anuncio.port.in.command.CadastrarVariacaoAnuncioCommand;

import java.util.List;

public class CadastrarAnuncioCommandMapper {

    public static List<Valor> toValores(List<CadastrarValorAnuncioCommand> valores) {
        return valores.stream()
                .map(v -> new Valor(v.valorUnidade(), v.quantidadeMinima()))
                .toList();
    }

    public static List<ImagemAnuncio> toImagens(List<CadastrarImagemAnuncioCommand> imagens) {
        return imagens.stream()
                .map(i -> new ImagemAnuncio(i.caminho(), i.principal()))
                .toList();
    }

    public static List<Variacao> toVariacoes(List<CadastrarVariacaoAnuncioCommand> variacoes) {
        return variacoes.stream()
                .map(v -> new Variacao(v.variacao()))
                .toList();
    }
}
