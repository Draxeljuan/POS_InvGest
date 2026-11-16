package com.invengest.auth.infrastructure.entry_points.dto;

public record ContactoUsuarioDTO(
        Integer idUsuario,
        String nombreCompleto,
        String email
) {}
