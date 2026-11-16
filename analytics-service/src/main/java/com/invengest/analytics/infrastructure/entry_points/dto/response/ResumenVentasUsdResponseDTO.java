package com.invengest.analytics.infrastructure.entry_points.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenVentasUsdResponseDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal totalVendidoCop;
    private BigDecimal totalAnuladoCop;
    private BigDecimal totalNetoCop;
    private BigDecimal totalVendidoUsd;
    private BigDecimal totalAnuladoUsd;
    private BigDecimal totalNetoUsd;
    private Integer cantidadOperaciones;
    private BigDecimal tasaCambioTrm;
    private LocalDate fechaVigenciaTrm;
    private String fuenteTrm;
}
