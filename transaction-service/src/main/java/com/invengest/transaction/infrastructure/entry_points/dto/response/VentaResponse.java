package com.invengest.transaction.infrastructure.entry_points.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
        Integer idVenta,
        Integer idUsuario,
        LocalDateTime fecha,
        ClienteResponse cliente,
        MetodoPagoResponse metodoPago,
        BigDecimal total,
        String estado,
        LocalDateTime fechaAnulacion,
        Integer idUsuarioAnulacion,
        String motivoAnulacion,
        List<DetalleVentaResponse> detalles
) {}
