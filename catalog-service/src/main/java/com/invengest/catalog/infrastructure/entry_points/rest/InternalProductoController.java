package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.SolicitudItemStock;
import com.invengest.catalog.domain.usecase.ProductoUseCase;
import com.invengest.catalog.infrastructure.entry_points.dto.request.ItemStockRequestDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ItemStockValidadoResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoResponse;
import com.invengest.catalog.infrastructure.mapper.ProductoRestMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/productos")
@RequiredArgsConstructor
public class InternalProductoController {

    private final ProductoUseCase productoUseCase;
    private final ProductoRestMapper productoRestMapper;

    @PostMapping("/validar-stock")
    public ResponseEntity<List<ItemStockValidadoResponseDTO>> validarStockBatch(
            @RequestBody @NotEmpty(message = "La lista de ítems a validar no puede estar vacía") List<@Valid ItemStockRequestDTO> items) {

        List<SolicitudItemStock> solicitudes = items.stream()
                .map(productoRestMapper::toSolicitudItemStock)
                .toList();

        List<ItemStockValidadoResponseDTO> respuestas = productoUseCase.validarStockBatch(solicitudes).stream()
                .map(productoRestMapper::toItemStockValidadoResponse)
                .toList();

        return ResponseEntity.ok(respuestas);
    }

    @PostMapping("/descontar-stock")
    public ResponseEntity<List<ProductoResponse>> descontarStockBatch(
            @RequestBody @NotEmpty(message = "La lista de ítems a descontar no puede estar vacía") List<@Valid ItemStockRequestDTO> items) {

        List<SolicitudItemStock> solicitudes = items.stream()
                .map(productoRestMapper::toSolicitudItemStock)
                .toList();

        List<ProductoResponse> respuestas = productoUseCase.descontarStockBatch(solicitudes).stream()
                .map(productoRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(respuestas);
    }

    @PostMapping("/reintegrar-stock")
    public ResponseEntity<List<ProductoResponse>> reintegrarStockBatch(
            @RequestBody @NotEmpty(message = "La lista de ítems a reintegrar no puede estar vacía") List<@Valid ItemStockRequestDTO> items) {

        List<SolicitudItemStock> solicitudes = items.stream()
                .map(productoRestMapper::toSolicitudItemStock)
                .toList();

        List<ProductoResponse> respuestas = productoUseCase.reintegrarStockBatch(solicitudes).stream()
                .map(productoRestMapper::toResponse)
                .toList();

        return ResponseEntity.ok(respuestas);
    }
}
