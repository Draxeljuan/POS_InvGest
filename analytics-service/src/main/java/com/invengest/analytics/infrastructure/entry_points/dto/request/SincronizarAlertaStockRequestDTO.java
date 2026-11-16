package com.invengest.analytics.infrastructure.entry_points.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SincronizarAlertaStockRequestDTO {

    @NotBlank(message = "El identificador del producto es obligatorio")
    private String idProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombreProducto;

    @NotNull(message = "El stock actual del producto es obligatorio")
    private Short stockActual;

    private Short stockMinimo;
    private String estado;
}
