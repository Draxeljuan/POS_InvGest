package com.invengest.analytics.infrastructure.driver_adapters.external_api.dto;

import java.math.BigDecimal;

public record DolarApiTrmResponseDTO(
        String unidad,
        String nombre,
        BigDecimal valor,
        String fechaActualizacion
) {}
