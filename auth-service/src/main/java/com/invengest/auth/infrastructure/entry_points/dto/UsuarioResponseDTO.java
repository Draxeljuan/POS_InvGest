package com.invengest.auth.infrastructure.entry_points.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Integer idUsuario,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String username,
        String rol,
        Boolean activo,
        LocalDateTime ultimoAcceso
) {}
