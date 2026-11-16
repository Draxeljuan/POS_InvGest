package com.invengest.transaction.infrastructure.entry_points.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ComprobanteVentaResponse(
        Integer idVenta,
        String numeroComprobante,
        LocalDateTime fecha,
        String estado,
        Integer idUsuario,
        String nombreCliente,
        String documentoCliente,
        String emailCliente,
        String metodoPago,
        BigDecimal total,
        List<DetalleVentaResponse> detalles
) {}
