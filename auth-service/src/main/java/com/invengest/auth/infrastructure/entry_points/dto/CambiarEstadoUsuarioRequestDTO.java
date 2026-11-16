package com.invengest.auth.infrastructure.entry_points.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoUsuarioRequestDTO(
        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo
) {}
