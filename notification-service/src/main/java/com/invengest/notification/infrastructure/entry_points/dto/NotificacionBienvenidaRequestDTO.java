package com.invengest.notification.infrastructure.entry_points.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NotificacionBienvenidaRequestDTO(
        @NotBlank(message = "El correo destinatario es obligatorio")
        @Email(message = "El formato del correo destinatario es inválido")
        String destinatario,

        @NotBlank(message = "El nombre del usuario es obligatorio")
        String nombre,

        @NotBlank(message = "El username es obligatorio")
        String username,

        @NotBlank(message = "El rol es obligatorio")
        String rol
) {}
