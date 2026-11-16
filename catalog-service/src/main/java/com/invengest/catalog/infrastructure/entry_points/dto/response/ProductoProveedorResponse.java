package com.invengest.catalog.infrastructure.entry_points.dto.response;

import java.math.BigDecimal;

public record ProductoProveedorResponse(
        String idProducto,
        Integer idProveedor,
        BigDecimal precioCompraProveedor
) {}
