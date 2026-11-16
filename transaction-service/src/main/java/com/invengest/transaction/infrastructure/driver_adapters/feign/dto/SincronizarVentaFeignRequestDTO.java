package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SincronizarVentaFeignRequestDTO(
        LocalDate fecha,
        BigDecimal totalVenta,
        List<ItemVentaAnalyticsFeignDTO> items
) {}
