package com.invengest.catalog.infrastructure.entry_points.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemStockRequestDTO(
        @NotBlank(message = "El código del producto es obligatorio") String idProducto,
        @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer cantidad
) {}
