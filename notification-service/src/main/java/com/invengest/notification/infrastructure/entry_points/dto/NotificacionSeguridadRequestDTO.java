package com.invengest.notification.infrastructure.entry_points.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NotificacionSeguridadRequestDTO(
        @NotBlank(message = "El correo destinatario es obligatorio")
        @Email(message = "El formato del correo destinatario es inválido")
        String destinatario,

        @NotBlank(message = "El nombre del usuario es obligatorio")
        String nombre,

        @NotBlank(message = "El evento de seguridad es obligatorio")
        String evento,

        String contrasenaTemporal
) {}
