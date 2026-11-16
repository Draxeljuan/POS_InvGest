package com.invengest.catalog.domain.model;

import java.math.BigDecimal;

public record ValidacionItemStock(
        String idProducto,
        String nombre,
        BigDecimal precioVenta,
        Integer stockDisponible,
        Integer cantidadSolicitada,
         boolean suficiente
) {}
