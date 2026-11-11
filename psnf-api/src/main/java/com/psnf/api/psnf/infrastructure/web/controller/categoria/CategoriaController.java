package com.psnf.api.psnf.infrastructure.web.controller.categoria;

import com.psnf.api.psnf.domain.categoria.port.in.BuscarTodasCategoriasUseCase;
import com.psnf.api.psnf.infrastructure.web.controller.categoria.dto.response.CategoriaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Categorias são fixas: carregadas pelo sistema (db/migration/R__categorias.sql) e somente leitura para
 * compradores e vendedores. A criação/edição ficará numa futura área administrativa, com permissão própria.
 */
@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final BuscarTodasCategoriasUseCase buscarTodasCategoriasUseCase;

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(buscarTodasCategoriasUseCase.buscarTodasCategorias().stream()
                .map(CategoriaResponse::from)
                .toList());
    }
}
