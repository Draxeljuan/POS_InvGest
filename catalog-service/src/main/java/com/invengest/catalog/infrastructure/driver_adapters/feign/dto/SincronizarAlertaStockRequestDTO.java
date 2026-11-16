package com.invengest.catalog.infrastructure.driver_adapters.feign.dto;

public record SincronizarAlertaStockRequestDTO(
        String idProducto,
        String nombreProducto,
        Short stockActual,
        Short stockMinimo,
        String estado
) {}
