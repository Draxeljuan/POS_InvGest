package com.invengest.analytics.infrastructure.entry_points.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SincronizarVentaRequestDTO {

    private LocalDate fecha;

    @NotNull(message = "El monto total de la venta es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto total de la venta no puede ser negativo")
    private BigDecimal totalVenta;

    private List<ItemVentaSincronizacionRequestDTO> items;
}
