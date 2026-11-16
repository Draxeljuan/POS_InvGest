package com.invengest.analytics.infrastructure.entry_points.rest;

import com.invengest.analytics.domain.model.DashboardResumen;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.usecase.ConsultarDashboardUseCase;
import com.invengest.analytics.infrastructure.entry_points.dto.response.DashboardResponseDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.response.ResumenVentaResponseDTO;
import com.invengest.analytics.infrastructure.mapper.DashboardRestMapper;
import com.invengest.analytics.infrastructure.mapper.ResumenVentaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ConsultarDashboardUseCase consultarDashboardUseCase;
    private final DashboardRestMapper dashboardRestMapper;
    private final ResumenVentaMapper resumenVentaMapper;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> obtenerResumenDashboard(
            @RequestParam(value = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "dias", required = false, defaultValue = "7") Integer dias) {
        DashboardResumen resumen = consultarDashboardUseCase.obtenerResumenDashboard(fecha, dias);
        return ResponseEntity.ok(dashboardRestMapper.toResponse(resumen));
    }

    @GetMapping("/ventas")
    public ResponseEntity<List<ResumenVentaResponseDTO>> consultarVentasPorRango(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        List<ResumenVentasDiarias> ventas = consultarDashboardUseCase.consultarVentasPorRango(inicio, fin);
        return ResponseEntity.ok(resumenVentaMapper.toResponseList(ventas));
    }
}
