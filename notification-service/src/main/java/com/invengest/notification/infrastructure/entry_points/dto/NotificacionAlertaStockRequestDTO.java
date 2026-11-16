package com.invengest.notification.infrastructure.entry_points.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record NotificacionAlertaStockRequestDTO(
        @NotEmpty(message = "Debe especificar al menos un destinatario")
        List<@NotBlank(message = "El correo destinatario no puede estar vacío") @Email(message = "Formato de correo destinatario inválido") String> destinatarios,

        @NotEmpty(message = "Debe incluir al menos un producto en alerta")
        @Valid
        List<ItemAlertaStockDTO> items
) {}
