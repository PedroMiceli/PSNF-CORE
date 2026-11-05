package com.psnf.api.psnf.infrastructure.web.controller.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
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
import java.util.UUID;

@RestController
@RequestMapping("/anuncios")
@RequiredArgsConstructor
public class AnuncioController {

    private final SalvarAnuncioUseCase salvarAnuncioUseCase;
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
        return ResponseEntity.ok(PaginaResponse.from(
                pesquisarAnunciosUseCase.pesquisar(new PesquisarAnunciosQuery(termo, categoriaId, pagina, tamanho)),
                AnuncioResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnuncioResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(AnuncioResponse.from(buscarAnuncioPorIdUseCase.buscarPorId(id)));
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
