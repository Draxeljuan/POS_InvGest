package com.invengest.catalog.infrastructure.entry_points.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductoRequest(
        String idProducto,
        @NotNull(message = "La categoría es obligatoria") Integer idCategoria,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotNull(message = "El precio de venta es obligatorio") @DecimalMin(value = "0.0", message = "El precio de venta no puede ser negativo") BigDecimal precioVenta,
        @NotNull(message = "El costo de compra es obligatorio") @DecimalMin(value = "0.0", message = "El costo de compra no puede ser negativo") BigDecimal costoCompra,
        @NotNull(message = "El stock es obligatorio") @Min(value = 0, message = "El stock no puede ser negativo") Integer stock,
        @NotNull(message = "El stock mínimo es obligatorio") @Min(value = 0, message = "El stock mínimo no puede ser negativo") Integer stockMinimo,
        String ubicacion,
        Integer idEstado
) {}
