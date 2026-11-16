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
public class ResumenVentaResponseDTO {
    private LocalDate fecha;
    private BigDecimal totalVentas;
    private BigDecimal totalAnulado;
    private BigDecimal ventasNetas;
    private Integer cantidadOperaciones;
}
