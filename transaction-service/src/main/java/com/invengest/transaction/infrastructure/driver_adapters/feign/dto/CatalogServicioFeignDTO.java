package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

import java.math.BigDecimal;

public record CatalogServicioFeignDTO(
        Integer idServicio,
        String nombre,
        String descripcion,
        BigDecimal precioSugerido
) {}
