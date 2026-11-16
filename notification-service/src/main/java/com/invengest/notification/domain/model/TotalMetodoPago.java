package com.invengest.notification.domain.model;

import java.math.BigDecimal;

public record TotalMetodoPago(
        String metodoPago,
        BigDecimal total
) {}
