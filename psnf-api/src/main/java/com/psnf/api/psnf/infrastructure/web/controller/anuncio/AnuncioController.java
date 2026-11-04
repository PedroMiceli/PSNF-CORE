package com.psnf.api.psnf.infrastructure.web.controller.anuncio;

import com.psnf.api.psnf.domain.anuncio.model.Anuncio;
import com.psnf.api.psnf.domain.anuncio.port.in.*;
import com.psnf.api.psnf.infrastructure.web.controller.UsuarioAutenticadoUtil;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.request.AnuncioRequest;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.response.AnuncioResponse;
import com.psnf.api.psnf.infrastructure.web.controller.anuncio.dto.response.AnuncioResumoResponse;
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
    private final ListarAnunciosAtivosUseCase listarAnunciosAtivosUseCase;

    @PostMapping
    public ResponseEntity<AnuncioResponse> salvar(@RequestBody @Valid AnuncioRequest request) {
        Anuncio anuncioSalvo = salvarAnuncioUseCase.salvarAnuncio(
                AnuncioRequestMapper.toCommand(request, UsuarioAutenticadoUtil.obterUsuarioLogadoId()));

        return ResponseEntity
                .created(URI.create("/anuncios/" + anuncioSalvo.getId()))
                .body(AnuncioResponse.from(anuncioSalvo));
    }

    /**
     * Vitrine: anúncios ativos (direto do banco). Ex.: GET /anuncios?categoriaId=...
     */
    @GetMapping
    public ResponseEntity<List<AnuncioResponse>> listarAtivos(@RequestParam(required = false) UUID categoriaId) {
        return ResponseEntity.ok(listarAnunciosAtivosUseCase.listarAtivos(categoriaId).stream()
                .map(AnuncioResponse::from)
                .toList());
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

    /**
     * Pesquisa no Elasticsearch. Ex.: GET /anuncios/pesquisa?termo=camiseta&categoriaId=...
     */
    @GetMapping("/pesquisa")
    public ResponseEntity<List<AnuncioResumoResponse>> pesquisar(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) UUID categoriaId
    ) {
        return ResponseEntity.ok(pesquisarAnunciosUseCase.pesquisar(termo, categoriaId).stream()
                .map(AnuncioResumoResponse::from)
                .toList());
    }
}
