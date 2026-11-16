package com.invengest.analytics.infrastructure.entry_points.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstacionalidadResponseDTO {
    private int anio;
    private int mes;
    private int diasHabiles;
    private int diasNoHabiles;
    private BigDecimal ventasDiasHabiles;
    private BigDecimal ventasDiasNoHabiles;
    private int operacionesDiasHabiles;
    private int operacionesDiasNoHabiles;
    private BigDecimal promedioVentasDiaHabil;
    private BigDecimal promedioVentasDiaNoHabil;
    private BigDecimal promedioOperacionesDiaHabil;
    private BigDecimal promedioOperacionesDiaNoHabil;
    private String fuenteFestivos;
}
