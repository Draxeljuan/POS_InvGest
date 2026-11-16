package com.invengest.analytics.infrastructure.entry_points.rest;

import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.ItemVentaSincronizacion;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.usecase.SincronizarAnaliticaUseCase;
import com.invengest.analytics.infrastructure.entry_points.dto.request.SincronizarAlertaStockRequestDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.request.SincronizarAnulacionVentaRequestDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.request.SincronizarVentaRequestDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.response.AlertaStockResponseDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.response.ResumenVentaResponseDTO;
import com.invengest.analytics.infrastructure.mapper.AlertaStockMapper;
import com.invengest.analytics.infrastructure.mapper.AnaliticaRestMapper;
import com.invengest.analytics.infrastructure.mapper.ResumenVentaMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/internal", "/api/analytics/internal"})
@RequiredArgsConstructor
public class SincronizacionAnaliticaController {

    private final SincronizarAnaliticaUseCase sincronizarAnaliticaUseCase;
    private final ResumenVentaMapper resumenVentaMapper;
    private final AlertaStockMapper alertaStockMapper;
    private final AnaliticaRestMapper analiticaRestMapper;

    @PostMapping("/ventas")
    public ResponseEntity<ResumenVentaResponseDTO> sincronizarVenta(
            @Valid @RequestBody SincronizarVentaRequestDTO request) {
        List<ItemVentaSincronizacion> items = request.getItems() != null
                ? analiticaRestMapper.toDomainItems(request.getItems())
                : Collections.emptyList();

        ResumenVentasDiarias resumenActualizado = sincronizarAnaliticaUseCase.registrarVentaSincronizada(
                request.getFecha(),
                request.getTotalVenta(),
                items
        );

        return ResponseEntity.ok(resumenVentaMapper.toResponse(resumenActualizado));
    }

    @PostMapping("/ventas/anulacion")
    public ResponseEntity<ResumenVentaResponseDTO> sincronizarAnulacionVenta(
            @Valid @RequestBody SincronizarAnulacionVentaRequestDTO request) {
        List<ItemVentaSincronizacion> items = request.getItems() != null
                ? analiticaRestMapper.toDomainItems(request.getItems())
                : Collections.emptyList();

        ResumenVentasDiarias resumenActualizado = sincronizarAnaliticaUseCase.registrarAnulacionSincronizada(
                request.obtenerFechaEfectiva(),
                request.getTotalAnulado(),
                items
        );

        return ResponseEntity.ok(resumenVentaMapper.toResponse(resumenActualizado));
    }

    @PostMapping("/alertas-stock")
    public ResponseEntity<AlertaStockResponseDTO> sincronizarAlertaStock(
            @Valid @RequestBody SincronizarAlertaStockRequestDTO request) {
        Optional<AlertaStock> alerta = sincronizarAnaliticaUseCase.sincronizarAlertaStock(
                request.getIdProducto(),
                request.getNombreProducto(),
                request.getStockActual(),
                request.getStockMinimo()
        );

        return alerta.map(a -> ResponseEntity.ok(alertaStockMapper.toResponse(a)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
