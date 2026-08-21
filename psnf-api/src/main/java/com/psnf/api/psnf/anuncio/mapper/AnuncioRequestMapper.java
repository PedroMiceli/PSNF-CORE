package com.psnf.api.psnf.anuncio.mapper;

import com.psnf.api.psnf.anuncio.application.DTO.request.AnuncioRequest;
import com.psnf.api.psnf.anuncio.application.DTO.request.ImagemAnuncioRequest;
import com.psnf.api.psnf.anuncio.application.DTO.request.ValorRequest;
import com.psnf.api.psnf.anuncio.application.DTO.request.VariacaoRequest;
import com.psnf.api.psnf.anuncio.domain.models.Anuncio;
import com.psnf.api.psnf.categoria.Categoria;
import com.psnf.api.psnf.categoria.adapters.outbound.CategoriaRepository;
import com.psnf.api.psnf.anuncio.domain.models.ImagemAnuncio;
import com.psnf.api.psnf.domain.models.Usuario;
import com.psnf.api.psnf.anuncio.domain.models.Valor;
import com.psnf.api.psnf.anuncio.domain.models.Variacao;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AnuncioRequestMapper {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public Anuncio toEntity(AnuncioRequest request, Usuario vendedor) {
        Anuncio anuncio = new Anuncio();
        anuncio.setTitulo(request.titulo());
        anuncio.setDescricao(request.descricao());
        anuncio.setAtivo(true);

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + request.categoriaId()));
        anuncio.setCategoria(categoria);

        anuncio.setVendedor(vendedor);

        anuncio.setValores(toValores(request.valores(), anuncio));
        anuncio.setImagens(toImagens(request.imagens(), anuncio));
        anuncio.setVariacoes(toVariacoes(request.variacoes(), anuncio));

        return anuncio;
    }

    private List<Valor> toValores(List<ValorRequest> requests, Anuncio anuncio) {
        if (requests == null) {
            return new ArrayList<>();
        }

        List<Valor> valores = new ArrayList<>();
        for (ValorRequest r : requests) {
            Valor valor = new Valor(r.valorUnidade(), r.quantidadeMinima());
            valor.setAnuncio(anuncio);
            valores.add(valor);
        }
        return valores;
    }

    private List<ImagemAnuncio> toImagens(List<ImagemAnuncioRequest> requests, Anuncio anuncio) {
        if (requests == null) {
            return new ArrayList<>();
        }

        List<ImagemAnuncio> imagens = new ArrayList<>();
        for (ImagemAnuncioRequest r : requests) {
            ImagemAnuncio imagem = new ImagemAnuncio();
            imagem.setCaminho(r.caminho());
            imagem.setPrincipal(r.principal());
            imagem.setAnuncio(anuncio);
            imagens.add(imagem);
        }
        return imagens;
    }

    private List<Variacao> toVariacoes(List<VariacaoRequest> requests, Anuncio anuncio) {
        if (requests == null) {
            return new ArrayList<>();
        }

        List<Variacao> variacoes = new ArrayList<>();
        for (VariacaoRequest r : requests) {
            Variacao variacao = new Variacao();
            variacao.setVariacao(r.variacao());
            variacao.setAnuncio(anuncio);
            variacoes.add(variacao);
        }
        return variacoes;
    }

}
