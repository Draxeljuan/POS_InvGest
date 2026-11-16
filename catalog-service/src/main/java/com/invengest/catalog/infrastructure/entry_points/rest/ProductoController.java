package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.MejorProveedorProducto;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.ProductoPrecioUsd;
import com.invengest.catalog.domain.usecase.ProductoUseCase;
import com.invengest.catalog.infrastructure.entry_points.dto.request.ProductoRequest;
import com.invengest.catalog.infrastructure.entry_points.dto.response.MejorProveedorResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoPrecioUsdResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoResponse;
import com.invengest.catalog.infrastructure.mapper.ProductoRestMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoUseCase productoUseCase;
    private final ProductoRestMapper productoRestMapper;

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarTodos() {
        List<ProductoResponse> responses = productoUseCase.obtenerTodos().stream()
                .map(productoRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponse>> buscarPorCriterio(@RequestParam String query) {
        List<ProductoResponse> responses = productoUseCase.buscarPorCriterio(query).stream()
                .map(productoRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoResponse>> obtenerStockBajo() {
        List<ProductoResponse> responses = productoUseCase.obtenerProductosStockBajo().stream()
                .map(productoRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/precios-usd")
    public ResponseEntity<List<ProductoPrecioUsdResponseDTO>> listarPreciosEnUsd() {
        List<ProductoPrecioUsdResponseDTO> responses = productoUseCase.listarPreciosEnUsd().stream()
                .map(productoRestMapper::toPrecioUsdResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}/precio-usd")
    public ResponseEntity<ProductoPrecioUsdResponseDTO> obtenerPrecioEnUsd(@PathVariable String id) {
        ProductoPrecioUsd precioUsd = productoUseCase.obtenerPrecioEnUsd(id);
        return ResponseEntity.ok(productoRestMapper.toPrecioUsdResponse(precioUsd));
    }

    @GetMapping("/{id}/mejor-proveedor")
    public ResponseEntity<MejorProveedorResponseDTO> obtenerMejorProveedor(@PathVariable String id) {
        MejorProveedorProducto mejor = productoUseCase.obtenerMejorProveedor(id);
        return ResponseEntity.ok(productoRestMapper.toMejorProveedorResponse(mejor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable String id) {
        Producto producto = productoUseCase.obtenerPorId(id);
        return ResponseEntity.ok(productoRestMapper.toResponse(producto));
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> registrar(@Valid @RequestBody ProductoRequest request) {
        Producto producto = productoRestMapper.toDomain(request);
        Producto registrado = productoUseCase.registrar(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoRestMapper.toResponse(registrado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable String id,
            @Valid @RequestBody ProductoRequest request) {
        Producto producto = productoRestMapper.toDomain(request);
        producto.setIdProducto(id);
        Producto actualizado = productoUseCase.actualizar(producto);
        return ResponseEntity.ok(productoRestMapper.toResponse(actualizado));
    }

    @PutMapping("/{id}/descontar-stock")
    public ResponseEntity<ProductoResponse> descontarStock(
            @PathVariable String id,
            @RequestParam Integer cantidad) {
        Producto actualizado = productoUseCase.descontarStock(id, cantidad);
        return ResponseEntity.ok(productoRestMapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        productoUseCase.eliminacionLogica(id);
        return ResponseEntity.noContent().build();
    }
}
