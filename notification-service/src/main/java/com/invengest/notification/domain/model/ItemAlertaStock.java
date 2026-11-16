package com.invengest.notification.domain.model;

public record ItemAlertaStock(
        String idProducto,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        String estado
) {}
