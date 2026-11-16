package com.invengest.transaction.infrastructure.entry_points.rest;

import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.ResumenCaja;
import com.invengest.transaction.domain.usecase.CajaUseCase;
import com.invengest.transaction.infrastructure.entry_points.dto.request.RegistrarCierreCajaRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.response.CierreCajaResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ResumenCajaResponse;
import com.invengest.transaction.infrastructure.mapper.CajaRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/transactions/caja", "/api/transaction/caja"})
@RequiredArgsConstructor
public class CajaController {

    private final CajaUseCase cajaUseCase;
    private final CajaRestMapper cajaRestMapper;

    @GetMapping("/resumen")
    public ResponseEntity<ResumenCajaResponse> obtenerResumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        ResumenCaja resumen = cajaUseCase.obtenerResumenCaja(fecha);
        return ResponseEntity.ok(cajaRestMapper.toResumenResponse(resumen));
    }

    @PostMapping("/cierre")
    public ResponseEntity<CierreCajaResponse> registrarCierre(
            @Valid @RequestBody RegistrarCierreCajaRequest request) {
        CierreCaja cierre = cajaUseCase.registrarCierreCaja(
                request.fechaCierre(),
                request.idUsuario(),
                request.efectivoContado(),
                request.observacion()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaRestMapper.toCierreResponse(cierre));
    }

    @GetMapping("/cierres/{fecha}")
    public ResponseEntity<CierreCajaResponse> obtenerCierrePorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        CierreCaja cierre = cajaUseCase.obtenerCierrePorFecha(fecha);
        return ResponseEntity.ok(cajaRestMapper.toCierreResponse(cierre));
    }

    @GetMapping("/cierres")
    public ResponseEntity<List<CierreCajaResponse>> listarCierres() {
        List<CierreCajaResponse> respuestas = cajaUseCase.listarCierres().stream()
                .map(cajaRestMapper::toCierreResponse)
                .toList();
        return ResponseEntity.ok(respuestas);
    }
}
