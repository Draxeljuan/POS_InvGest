package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

import java.math.BigDecimal;

public record CatalogProductoFeignDTO(
        String idProducto,
        String nombre,
        BigDecimal precioVenta,
        Integer stock,
        Integer stockMinimo
) {}
