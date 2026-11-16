package com.invengest.catalog.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoPrecioUsd(
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
