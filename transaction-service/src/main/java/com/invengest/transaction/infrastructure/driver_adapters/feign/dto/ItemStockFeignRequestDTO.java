package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

public record ItemStockFeignRequestDTO(
        String idProducto,
        Integer cantidad
) {}
