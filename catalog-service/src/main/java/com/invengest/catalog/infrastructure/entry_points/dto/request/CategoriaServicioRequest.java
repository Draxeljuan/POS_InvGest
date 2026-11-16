package com.invengest.catalog.infrastructure.entry_points.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoriaServicioRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre
) {}
