package com.invengest.catalog.infrastructure.entry_points.dto.response;

import java.math.BigDecimal;

public record ItemStockValidadoResponseDTO(
        String idProducto,
        String nombre,
        BigDecimal precioVenta,
        Integer stockDisponible,
        Integer cantidadSolicitada,
        boolean suficiente
) {}
