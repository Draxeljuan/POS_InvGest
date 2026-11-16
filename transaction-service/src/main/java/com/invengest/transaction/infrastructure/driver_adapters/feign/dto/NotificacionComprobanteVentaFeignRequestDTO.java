package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record NotificacionComprobanteVentaFeignRequestDTO(
        String destinatario,
        String nombreCliente,
        Integer idVenta,
        LocalDateTime fecha,
        List<ItemComprobanteFeignDTO> items,
        BigDecimal total,
        String metodoPago
) {}
