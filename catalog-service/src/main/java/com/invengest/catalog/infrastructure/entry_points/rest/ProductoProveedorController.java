package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.ProductoProveedor;
import com.invengest.catalog.domain.usecase.ProductoProveedorUseCase;
import com.invengest.catalog.infrastructure.entry_points.dto.request.ProductoProveedorRequest;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoProveedorResponse;
import com.invengest.catalog.infrastructure.mapper.ProductoProveedorRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/productos-proveedores")
@RequiredArgsConstructor
public class ProductoProveedorController {

    private final ProductoProveedorUseCase productoProveedorUseCase;
    private final ProductoProveedorRestMapper productoProveedorRestMapper;

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<ProductoProveedorResponse>> obtenerPorProducto(@PathVariable String idProducto) {
        List<ProductoProveedorResponse> responses = productoProveedorUseCase.obtenerPorProducto(idProducto).stream()
                .map(productoProveedorRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/proveedor/{idProveedor}")
    public ResponseEntity<List<ProductoProveedorResponse>> obtenerPorProveedor(@PathVariable Integer idProveedor) {
        List<ProductoProveedorResponse> responses = productoProveedorUseCase.obtenerPorProveedor(idProveedor).stream()
                .map(productoProveedorRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ProductoProveedorResponse> asociarOActualizar(
            @Valid @RequestBody ProductoProveedorRequest request) {
        ProductoProveedor productoProveedor = productoProveedorRestMapper.toDomain(request);
        ProductoProveedor asociado = productoProveedorUseCase.asociarOActualizar(productoProveedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoProveedorRestMapper.toResponse(asociado));
    }

    @DeleteMapping("/producto/{idProducto}/proveedor/{idProveedor}")
    public ResponseEntity<Void> eliminarAsociacion(
            @PathVariable String idProducto,
            @PathVariable Integer idProveedor) {
        productoProveedorUseCase.eliminarAsociacion(idProducto, idProveedor);
        return ResponseEntity.noContent().build();
    }
}
