package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.usecase.EstadoProveedorUseCase;
import com.invengest.catalog.infrastructure.entry_points.dto.response.EstadoProveedorResponse;
import com.invengest.catalog.infrastructure.mapper.EstadoProveedorRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/estados-proveedor")
@RequiredArgsConstructor
public class EstadoProveedorController {

    private final EstadoProveedorUseCase estadoProveedorUseCase;
    private final EstadoProveedorRestMapper estadoProveedorRestMapper;

    @GetMapping
    public ResponseEntity<List<EstadoProveedorResponse>> obtenerTodos() {
        List<EstadoProveedorResponse> responses = estadoProveedorUseCase.obtenerTodos().stream()
                .map(estadoProveedorRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstadoProveedorResponse> obtenerPorId(@PathVariable Integer id) {
        EstadoProveedor estado = estadoProveedorUseCase.obtenerPorId(id);
        return ResponseEntity.ok(estadoProveedorRestMapper.toResponse(estado));
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<EstadoProveedorResponse> obtenerPorNombre(@PathVariable String nombre) {
        EstadoProveedor estado = estadoProveedorUseCase.obtenerPorNombre(nombre);
        return ResponseEntity.ok(estadoProveedorRestMapper.toResponse(estado));
    }
}
