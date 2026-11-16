package com.invengest.notification.infrastructure.entry_points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TotalMetodoPagoDTO(
        @NotBlank(message = "El nombre del método de pago es obligatorio")
        String metodoPago,

        @NotNull(message = "El monto total por método es obligatorio")
        BigDecimal total
) {}
