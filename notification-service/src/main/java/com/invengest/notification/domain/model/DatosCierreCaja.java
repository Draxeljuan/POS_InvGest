package com.invengest.notification.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record DatosCierreCaja(
        String destinatario,
        String fecha,
        List<TotalMetodoPago> totalesPorMetodo,
        BigDecimal esperado,
        BigDecimal contado,
        BigDecimal diferencia,
        Boolean esDiaHabil,
        String proximoDiaHabil
) {}
