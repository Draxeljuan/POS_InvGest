package com.invengest.notification.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record DatosComprobanteVenta(
        String destinatario,
        String nombreCliente,
        Integer idVenta,
        String fecha,
        List<ItemComprobante> items,
        BigDecimal total,
        String metodoPago
) {}
