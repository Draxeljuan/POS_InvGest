package com.invengest.catalog.infrastructure.driver_adapters.feign.dto;

public record ItemAlertaStockFeignDTO(
        String idProducto,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        String estado
) {}
