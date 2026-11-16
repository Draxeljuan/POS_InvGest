package com.invengest.catalog.infrastructure.entry_points.dto.response;

public record CategoriaResponse(
        Integer idCategoria,
        String nombre,
        String descripcion
) {}
