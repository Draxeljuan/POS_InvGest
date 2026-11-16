package com.invengest.transaction.infrastructure.entry_points.dto.response;

import java.math.BigDecimal;

public record CierreCajaDetalleResponse(
        Integer idDetalle,
        MetodoPagoResponse metodoPago,
        BigDecimal totalMetodo,
        Integer cantidadOperaciones
) {}
