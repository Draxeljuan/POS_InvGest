package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

import java.math.BigDecimal;

public record ItemComprobanteFeignDTO(
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}
