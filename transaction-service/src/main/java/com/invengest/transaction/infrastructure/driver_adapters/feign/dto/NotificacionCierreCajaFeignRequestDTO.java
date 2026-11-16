package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NotificacionCierreCajaFeignRequestDTO(
        String destinatario,
        LocalDate fecha,
        List<TotalMetodoPagoFeignDTO> totalesPorMetodo,
        BigDecimal esperado,
        BigDecimal contado,
        BigDecimal diferencia,
        Boolean esDiaHabil,
        LocalDate proximoDiaHabil
) {}
