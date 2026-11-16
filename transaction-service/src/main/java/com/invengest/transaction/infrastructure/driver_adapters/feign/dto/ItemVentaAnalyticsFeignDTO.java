package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

public record ItemVentaAnalyticsFeignDTO(
        String idProducto,
        String nombreProducto,
        Integer cantidadVendida
) {}
