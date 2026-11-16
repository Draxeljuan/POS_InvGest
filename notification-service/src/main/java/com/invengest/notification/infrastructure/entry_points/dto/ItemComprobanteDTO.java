package com.invengest.notification.infrastructure.entry_points.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemComprobanteDTO(
        @NotBlank(message = "El nombre del ítem es obligatorio")
        String nombre,

        @NotNull(message = "La cantidad del ítem es obligatoria")
        @Min(value = 1, message = "La cantidad del ítem debe ser al menos 1")
        Integer cantidad,

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio unitario no puede ser negativo")
        BigDecimal precioUnitario,

        @NotNull(message = "El subtotal del ítem es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El subtotal no puede ser negativo")
        BigDecimal subtotal
) {}
