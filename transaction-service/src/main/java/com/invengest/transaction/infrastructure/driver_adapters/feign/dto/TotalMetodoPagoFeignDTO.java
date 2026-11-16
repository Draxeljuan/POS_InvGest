package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

import java.math.BigDecimal;

public record TotalMetodoPagoFeignDTO(
        String metodoPago,
        BigDecimal total,
        Integer cantidadOperaciones
) {}
