package com.invengest.notification.infrastructure.entry_points.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record NotificacionCierreCajaRequestDTO(
        @NotBlank(message = "El correo destinatario es obligatorio")
        @Email(message = "El formato del correo destinatario es inválido")
        String destinatario,

        @NotBlank(message = "La fecha de cierre es obligatoria")
        String fecha,

        @Valid
        List<TotalMetodoPagoDTO> totalesPorMetodo,

        @NotNull(message = "El monto esperado es obligatorio")
        BigDecimal esperado,

        @NotNull(message = "El monto contado es obligatorio")
        BigDecimal contado,

        @NotNull(message = "La diferencia de arqueo es obligatoria")
        BigDecimal diferencia,

        Boolean esDiaHabil,

        String proximoDiaHabil
) {}
