package com.invengest.notification.domain.model;

import java.math.BigDecimal;

public record ItemComprobante(
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}
