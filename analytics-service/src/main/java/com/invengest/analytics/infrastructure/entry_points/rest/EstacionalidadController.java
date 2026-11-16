package com.invengest.analytics.infrastructure.entry_points.rest;

import com.invengest.analytics.domain.model.ReporteEstacionalidad;
import com.invengest.analytics.domain.usecase.ConsultarEstacionalidadUseCase;
import com.invengest.analytics.infrastructure.entry_points.dto.response.EstacionalidadResponseDTO;
import com.invengest.analytics.infrastructure.mapper.AnaliticaRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/estacionalidad")
@RequiredArgsConstructor
public class EstacionalidadController {

    private final ConsultarEstacionalidadUseCase consultarEstacionalidadUseCase;
    private final AnaliticaRestMapper analiticaRestMapper;

    @GetMapping
    public ResponseEntity<EstacionalidadResponseDTO> obtenerEstacionalidadMensual(
            @RequestParam("anio") int anio,
            @RequestParam("mes") int mes) {
        ReporteEstacionalidad reporte = consultarEstacionalidadUseCase.analizarEstacionalidadMensual(anio, mes);
        return ResponseEntity.ok(analiticaRestMapper.toEstacionalidadResponse(reporte));
    }
}
