package com.invengest.catalog.infrastructure.entry_points.dto.response;

import java.math.BigDecimal;

public record MejorProveedorResponseDTO(
        String idProducto,
        String nombreProducto,
        BigDecimal precioVentaActual,
        Integer idProveedor,
        String nombreProveedor,
        String nitProveedor,
        String telefonoProveedor,
        String emailProveedor,
        BigDecimal precioCompraProveedor,
        BigDecimal margenUnitarioEstimado,
        BigDecimal porcentajeMargenEstimado
) {}
