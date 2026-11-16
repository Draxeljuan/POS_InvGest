package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.domain.usecase.ProveedorUseCase;
import com.invengest.catalog.infrastructure.entry_points.dto.request.ProveedorRequest;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProveedorResponse;
import com.invengest.catalog.infrastructure.mapper.ProveedorRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorUseCase proveedorUseCase;
    private final ProveedorRestMapper proveedorRestMapper;

    @GetMapping
    public ResponseEntity<List<ProveedorResponse>> listarTodos() {
        List<ProveedorResponse> responses = proveedorUseCase.obtenerTodos().stream()
                .map(proveedorRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponse> obtenerPorId(@PathVariable Integer id) {
        Proveedor proveedor = proveedorUseCase.obtenerPorId(id);
        return ResponseEntity.ok(proveedorRestMapper.toResponse(proveedor));
    }

    @PostMapping
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        Proveedor proveedor = proveedorRestMapper.toDomain(request);
        Proveedor creado = proveedorUseCase.crear(proveedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorRestMapper.toResponse(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProveedorRequest request) {
        Proveedor proveedor = proveedorRestMapper.toDomain(request);
        proveedor.setIdProveedor(id);
        Proveedor actualizado = proveedorUseCase.actualizar(proveedor);
        return ResponseEntity.ok(proveedorRestMapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        proveedorUseCase.eliminacionLogica(id);
        return ResponseEntity.noContent().build();
    }
}
