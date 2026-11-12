package com.psnf.api.psnf.infrastructure.web.controller.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.model.CategoriaComAnuncios;
import com.psnf.api.psnf.domain.avaliacao.model.ResumoAvaliacoes;
import com.psnf.api.psnf.domain.avaliacao.port.in.AvaliarAnuncioUseCase;
import com.psnf.api.psnf.domain.anuncio.port.in.*;
import com.psnf.api.psnf.domain.anuncio.port.in.command.PesquisarAnunciosQuery;
import com.psnf.api.psnf.infrastructure.web.controller.UsuarioAutenticadoUtil;
import com.psnf.api.psnf.infrastructure.web.controller.dto.PaginaResponse;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request.AnuncioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.response.AnuncioResponse;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.mapper.AnuncioRequestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/anuncios")
@RequiredArgsConstructor
public class AnuncioController {

    private final SalvarAnuncioUseCase salvarAnuncioUseCase;
    private final AvaliarAnuncioUseCase avaliarAnuncioUseCase;
    private final AtualizarAnuncioUseCase atualizarAnuncioUseCase;
    private final AlterarStatusAnuncioUseCase alterarStatusAnuncioUseCase;
    private final BuscarAnuncioPorIdUseCase buscarAnuncioPorIdUseCase;
    private final ListarAnunciosPorVendedorUseCase listarAnunciosPorVendedorUseCase;
    private final PesquisarAnunciosUseCase pesquisarAnunciosUseCase;

    @PostMapping
    public ResponseEntity<AnuncioResponse> salvar(@RequestBody @Valid AnuncioRequest request) {
        Anuncio anuncioSalvo = salvarAnuncioUseCase.salvarAnuncio(
                AnuncioRequestMapper.toCommand(request, UsuarioAutenticadoUtil.obterUsuarioLogadoId()));

        return ResponseEntity
                .created(URI.create("/anuncios/" + anuncioSalvo.getId()))
                .body(AnuncioResponse.from(anuncioSalvo));
    }

    /**
     * Vitrine e pesquisa: anúncios ativos, paginados. A busca de texto é feita no PostgreSQL.
     * PÚBLICA (sem login): todos veem todos os anúncios. Abrir o detalhe exige login; negociar um anúncio
     * "somente para empresas" exige CNPJ.
     * Ex.: GET /anuncios?termo=carrinho&categoriaId=...&pagina=0&tamanho=24
     * Sem termo: mais recentes primeiro. Com termo: mais relevantes primeiro.
     */
    @GetMapping
    public ResponseEntity<PaginaResponse<AnuncioResponse>> pesquisar(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + PesquisarAnunciosQuery.TAMANHO_PADRAO) int tamanho
    ) {
        return ResponseEntity.ok(pesquisarComNotas(new PesquisarAnunciosQuery(termo, categoriaId, pagina, tamanho)));
    }

    /**
     * Página do vendedor: os anúncios ATIVOS dele, com a mesma pesquisa e paginação da vitrine.
     * Exige login (só GET /anuncios, exatamente, é público): quem não está logado não navega pelo catálogo
     * de um fornecedor. Ex.: GET /anuncios/vendedor/{vendedorId}?termo=cabo&categoriaId=...&pagina=0
     */
    @GetMapping("/vendedor/{vendedorId}")
    public ResponseEntity<PaginaResponse<AnuncioResponse>> anunciosDoVendedor(
            @PathVariable UUID vendedorId,
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + PesquisarAnunciosQuery.TAMANHO_PADRAO) int tamanho
    ) {
        return ResponseEntity.ok(pesquisarComNotas(new PesquisarAnunciosQuery(termo, categoriaId, vendedorId, pagina, tamanho)));
    }

    /** Categorias em que o vendedor tem anúncios ativos, com a quantidade (filtro da página do vendedor). */
    @GetMapping("/vendedor/{vendedorId}/categorias")
    public ResponseEntity<List<CategoriaComAnuncios>> categoriasDoVendedor(@PathVariable UUID vendedorId) {
        return ResponseEntity.ok(pesquisarAnunciosUseCase.categoriasDoVendedor(vendedorId));
    }

    private PaginaResponse<AnuncioResponse> pesquisarComNotas(PesquisarAnunciosQuery query) {
        var resultado = pesquisarAnunciosUseCase.pesquisar(query);
        // notas da página inteira de uma vez (uma consulta)
        Map<UUID, ResumoAvaliacoes> notas = avaliarAnuncioUseCase.resumoPorAnuncios(resultado.itens().stream().map(Anuncio::getId).toList());
        return PaginaResponse.from(resultado, a -> AnuncioResponse.from(a, notas.get(a.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnuncioResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(AnuncioResponse.from(buscarAnuncioPorIdUseCase.buscarPorId(id),
                avaliarAnuncioUseCase.resumoPorAnuncios(List.of(id)).get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnuncioResponse> atualizar(@PathVariable UUID id, @RequestBody @Valid AnuncioRequest request) {
        Anuncio anuncio = atualizarAnuncioUseCase.atualizarAnuncio(
                AnuncioRequestMapper.toCommand(id, UsuarioAutenticadoUtil.obterUsuarioLogadoId(), request));
        return ResponseEntity.ok(AnuncioResponse.from(anuncio));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<AnuncioResponse> ativar(@PathVariable UUID id) {
        return ResponseEntity.ok(AnuncioResponse.from(
                alterarStatusAnuncioUseCase.ativar(id, UsuarioAutenticadoUtil.obterUsuarioLogadoId())));
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<AnuncioResponse> desativar(@PathVariable UUID id) {
        return ResponseEntity.ok(AnuncioResponse.from(
                alterarStatusAnuncioUseCase.desativar(id, UsuarioAutenticadoUtil.obterUsuarioLogadoId())));
    }

    @GetMapping("/meus")
    public ResponseEntity<List<AnuncioResponse>> meusAnuncios() {
        return ResponseEntity.ok(listarAnunciosPorVendedorUseCase
                .listarPorVendedor(UsuarioAutenticadoUtil.obterUsuarioLogadoId()).stream()
                .map(AnuncioResponse::from)
                .toList());
    }
}
