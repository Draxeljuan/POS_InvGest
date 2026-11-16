package com.invengest.notification.infrastructure.entry_points.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemAlertaStockDTO(
        @NotBlank(message = "El código del producto es obligatorio")
        String idProducto,

        @NotBlank(message = "El nombre del producto es obligatorio")
        String nombre,

        @NotNull(message = "El stock actual es obligatorio")
        @Min(value = 0, message = "El stock actual no puede ser negativo")
        Integer stockActual,

        @NotNull(message = "El stock mínimo es obligatorio")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo,

        @NotBlank(message = "El estado del producto es obligatorio")
        String estado
) {}
