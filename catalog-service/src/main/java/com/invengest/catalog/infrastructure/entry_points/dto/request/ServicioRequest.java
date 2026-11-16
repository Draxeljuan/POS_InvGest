package com.invengest.catalog.infrastructure.entry_points.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ServicioRequest(
        @NotNull(message = "La categoría del servicio es obligatoria") Integer idCategoriaServicio,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        String descripcion,
        @NotNull(message = "El precio sugerido es obligatorio") @DecimalMin(value = "0.0", message = "El precio sugerido no puede ser negativo") BigDecimal precioSugerido
) {}
