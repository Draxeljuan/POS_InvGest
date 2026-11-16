package com.invengest.transaction.infrastructure.entry_points.dto.request;

import jakarta.validation.constraints.Email;

public record EnviarComprobanteRequest(
        @Email(message = "El correo electrónico de destino no tiene un formato válido")
        String emailDestino
) {}
