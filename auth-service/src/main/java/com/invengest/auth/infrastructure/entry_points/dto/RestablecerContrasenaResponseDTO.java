package com.invengest.auth.infrastructure.entry_points.dto;

public record RestablecerContrasenaResponseDTO(
        Integer idUsuario,
        String mensaje,
        String contrasenaTemporal
) {}
