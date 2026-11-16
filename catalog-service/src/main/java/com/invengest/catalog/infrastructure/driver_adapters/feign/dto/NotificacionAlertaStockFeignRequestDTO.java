package com.invengest.catalog.infrastructure.driver_adapters.feign.dto;

import java.util.List;

public record NotificacionAlertaStockFeignRequestDTO(
        List<String> destinatarios,
        List<ItemAlertaStockFeignDTO> items
) {}
