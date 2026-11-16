package com.invengest.notification.infrastructure.entry_points.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record NotificacionComprobanteVentaRequestDTO(
        @NotBlank(message = "El correo destinatario es obligatorio")
        @Email(message = "El formato del correo destinatario es inválido")
        String destinatario,

        String nombreCliente,

        @NotNull(message = "El identificador de la venta es obligatorio")
        @Min(value = 1, message = "El identificador de la venta debe ser positivo")
        Integer idVenta,

        String fecha,

        @NotEmpty(message = "Debe incluir al menos un ítem en el comprobante")
        @Valid
        List<ItemComprobanteDTO> items,

        @NotNull(message = "El total de la venta es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El total no puede ser negativo")
        BigDecimal total,

        @NotBlank(message = "El método de pago es obligatorio")
        String metodoPago
) {}
