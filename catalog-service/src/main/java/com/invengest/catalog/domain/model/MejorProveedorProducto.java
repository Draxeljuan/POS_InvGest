package com.invengest.catalog.domain.model;

import java.math.BigDecimal;

public record MejorProveedorProducto(
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
