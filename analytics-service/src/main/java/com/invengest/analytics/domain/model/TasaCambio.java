package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record TasaCambio(
        BigDecimal valorCopPorUsd,
        LocalDate fechaVigencia,
        String fuente
) {
    public static final String FUENTE_API_LIVE = "API_LIVE";
    public static final String FUENTE_CACHE = "CACHE";
    public static final String FUENTE_CACHE_STALE = "CACHE_STALE";

    public TasaCambio {
        if (valorCopPorUsd == null || valorCopPorUsd.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El valor de la TRM (COP por USD) debe ser mayor a cero.");
        }
        if (fechaVigencia == null) {
            fechaVigencia = LocalDate.now();
        }
        if (fuente == null || fuente.isBlank()) {
            fuente = FUENTE_API_LIVE;
        }
    }

    public BigDecimal convertirAUsd(BigDecimal montoCop) {
        if (montoCop == null || montoCop.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El monto en COP a convertir no puede ser nulo ni negativo.");
        }
        return montoCop.divide(valorCopPorUsd, 2, RoundingMode.HALF_EVEN);
    }
}
