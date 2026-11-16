package com.invengest.analytics.infrastructure.entry_points.dto.request;

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
public class ItemVentaSincronizacionRequestDTO {

    private String idProducto;
    private String nombreProducto;
    private Integer cantidadVendida;
    private Integer cantidadAnulada;

    public Integer obtenerCantidadEfectiva() {
        return cantidadVendida != null ? cantidadVendida : cantidadAnulada;
    }
}
