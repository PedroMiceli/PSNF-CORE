package com.psnf.api.psnf.anuncio.adapters.outbound.entities;

import com.psnf.api.psnf.anuncio.domain.models.ImagemAnuncio;
import com.psnf.api.psnf.anuncio.domain.models.Valor;
import com.psnf.api.psnf.anuncio.domain.models.Variacao;
import com.psnf.api.psnf.categoria.Categoria;
import com.psnf.api.psnf.domain.models.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class JPAAnuncio extends BaseEntity{

    private String titulo;
    private String descricao;
    private boolean ativo;

    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JPAValor> valores = new ArrayList<>();

    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JPAImagemAnuncio> imagens = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JPAVariacao> variacoes = new ArrayList<>();
}
