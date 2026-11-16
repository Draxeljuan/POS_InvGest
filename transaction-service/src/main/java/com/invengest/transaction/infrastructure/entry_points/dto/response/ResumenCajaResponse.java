package com.invengest.transaction.infrastructure.entry_points.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenCajaResponse(
        LocalDate fecha,
        BigDecimal totalVentasCompletadas,
        Integer cantidadVentasCompletadas,
        BigDecimal totalVentasAnuladas,
        Integer cantidadVentasAnuladas,
        BigDecimal efectivoEsperado,
        List<ResumenMetodoPagoResponse> totalesPorMetodoPago,
        Boolean cierreYaRegistrado
) {}
