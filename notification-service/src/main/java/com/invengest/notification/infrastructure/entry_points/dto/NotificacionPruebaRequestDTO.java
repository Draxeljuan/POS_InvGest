package com.invengest.notification.infrastructure.entry_points.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NotificacionPruebaRequestDTO(
        @NotBlank(message = "El correo destinatario es obligatorio")
        @Email(message = "El formato del correo destinatario es inválido")
        String destinatario
) {}
