package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SincronizarAnulacionVentaFeignRequestDTO(
        Integer idVenta,
        LocalDate fechaVenta,
        BigDecimal totalAnulado,
        String motivo,
        List<ItemVentaAnalyticsFeignDTO> items
) {}
