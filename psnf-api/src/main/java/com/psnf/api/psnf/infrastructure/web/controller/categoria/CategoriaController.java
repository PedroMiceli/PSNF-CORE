package com.psnf.api.psnf.infrastructure.web.controller.categoria;

import com.psnf.api.psnf.domain.categoria.model.Categoria;
import com.psnf.api.psnf.domain.categoria.port.in.BuscarTodasCategoriasUseCase;
import com.psnf.api.psnf.domain.categoria.port.in.CadastrarCategoriaUseCase;
import com.psnf.api.psnf.domain.categoria.port.in.command.CadastrarCategoriaCommand;
import com.psnf.api.psnf.infrastructure.web.controller.categoria.dto.request.CategoriaRequest;
import com.psnf.api.psnf.infrastructure.web.controller.categoria.dto.response.CategoriaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CadastrarCategoriaUseCase cadastrarCategoriaUseCase;
    private final BuscarTodasCategoriasUseCase buscarTodasCategoriasUseCase;

    @PostMapping
    public ResponseEntity<CategoriaResponse> cadastrar(@RequestBody @Valid CategoriaRequest request) {
        Categoria categoria = cadastrarCategoriaUseCase.cadastrarCategoria(new CadastrarCategoriaCommand(request.nome()));
        return ResponseEntity
                .created(URI.create("/categorias/" + categoria.getId()))
                .body(CategoriaResponse.from(categoria));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(buscarTodasCategoriasUseCase.buscarTodasCategorias().stream()
                .map(CategoriaResponse::from)
                .toList());
    }
}
