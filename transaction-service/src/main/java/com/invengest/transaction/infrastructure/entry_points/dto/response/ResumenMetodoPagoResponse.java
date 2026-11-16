package com.invengest.transaction.infrastructure.entry_points.dto.response;

import java.math.BigDecimal;

public record ResumenMetodoPagoResponse(
        Integer idMetodoPago,
        String nombreMetodoPago,
        BigDecimal total,
        Integer cantidadOperaciones
) {}
