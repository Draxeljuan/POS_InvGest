package com.invengest.catalog.infrastructure.entry_points.dto.response;

import java.math.BigDecimal;

public record ServicioResponse(
        Integer idServicio,
        CategoriaServicioResponse categoriaServicio,
        String nombre,
        String descripcion,
        BigDecimal precioSugerido
) {}
