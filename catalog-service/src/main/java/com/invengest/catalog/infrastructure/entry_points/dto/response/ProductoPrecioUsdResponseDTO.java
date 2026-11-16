package com.invengest.catalog.infrastructure.entry_points.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoPrecioUsdResponseDTO(
        String idProducto,
        String nombre,
        String categoria,
        BigDecimal precioVentaCop,
        BigDecimal precioVentaUsd,
        BigDecimal trmVigente,
        LocalDate fechaTrm,
        String fuenteTrm,
        Integer stock,
        String estado
) {}
