package com.invengest.catalog.infrastructure.entry_points.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductoProveedorRequest(
        @NotBlank(message = "El id de producto es obligatorio") String idProducto,
        @NotNull(message = "El id de proveedor es obligatorio") Integer idProveedor,
        @NotNull(message = "El precio de compra es obligatorio") @DecimalMin(value = "0.0", message = "El precio de compra no puede ser negativo") BigDecimal precioCompraProveedor
) {}
