package com.invengest.transaction.infrastructure.entry_points.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CierreCajaResponse(
        Integer idCierre,
        LocalDate fechaCierre,
        Integer idUsuario,
        BigDecimal totalVentas,
        Integer cantidadVentas,
        BigDecimal efectivoEsperado,
        BigDecimal efectivoContado,
        BigDecimal diferencia,
        Boolean esDiaHabil,
        LocalDate proximoDiaHabil,
        String observacion,
        LocalDateTime fechaRegistro,
        List<CierreCajaDetalleResponse> detalles
) {}
