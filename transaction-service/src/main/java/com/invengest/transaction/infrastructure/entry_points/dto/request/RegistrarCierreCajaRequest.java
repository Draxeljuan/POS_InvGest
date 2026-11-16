package com.invengest.transaction.infrastructure.entry_points.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarCierreCajaRequest(
        LocalDate fechaCierre,

        @NotNull(message = "El ID del usuario responsable del cierre es obligatorio")
        Integer idUsuario,

        @NotNull(message = "El efectivo contado es obligatorio")
        @DecimalMin(value = "0.00", message = "El efectivo contado no puede ser negativo")
        BigDecimal efectivoContado,

        @Size(max = 255, message = "La observación no puede exceder 255 caracteres")
        String observacion
) {}
