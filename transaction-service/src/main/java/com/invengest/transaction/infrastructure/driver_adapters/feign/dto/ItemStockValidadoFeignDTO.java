package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

import java.math.BigDecimal;

public record ItemStockValidadoFeignDTO(
        String idProducto,
        String nombre,
        BigDecimal precioVenta,
        Integer stockDisponible,
        Integer cantidadSolicitada,
        boolean suficiente
) {}
