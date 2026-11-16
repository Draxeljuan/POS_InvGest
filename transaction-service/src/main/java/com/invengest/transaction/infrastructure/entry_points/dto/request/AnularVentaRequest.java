package com.invengest.transaction.infrastructure.entry_points.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnularVentaRequest(
        @NotNull(message = "El ID del usuario que anula la venta es obligatorio")
        Integer idUsuarioAnulacion,

        @NotBlank(message = "El motivo de anulación es obligatorio")
        @Size(max = 255, message = "El motivo de anulación no puede exceder 255 caracteres")
        String motivoAnulacion
) {}
