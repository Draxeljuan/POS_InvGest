package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProducto {

    private Integer idTop;
    private String idProducto;
    private String nombreProducto;
    private Integer cantidadVendida;
    private LocalDate fecha;

    public static TopProducto crearInicial(String idProducto, String nombreProducto, LocalDate fecha) {
        if (idProducto == null || idProducto.isBlank()) {
            throw new ReglaNegocioException("El identificador del producto es obligatorio para el ranking.");
        }
        return TopProducto.builder()
                .idProducto(idProducto.trim())
                .nombreProducto(nombreProducto != null && !nombreProducto.isBlank() ? nombreProducto.trim() : idProducto.trim())
                .cantidadVendida(0)
                .fecha(fecha != null ? fecha : LocalDate.now())
                .build();
    }

    public void incrementarCantidad(int cantidad, String nuevoNombre) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad vendida a incrementar debe ser mayor a cero.");
        }
        int base = cantidadVendida != null ? cantidadVendida : 0;
        this.cantidadVendida = base + cantidad;
        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            this.nombreProducto = nuevoNombre.trim();
        }
    }

    public void descontarAnulacion(int cantidadAnulada) {
        if (cantidadAnulada <= 0) {
            throw new ReglaNegocioException("La cantidad anulada a descontar debe ser mayor a cero.");
        }
        int base = cantidadVendida != null ? cantidadVendida : 0;
        this.cantidadVendida = Math.max(0, base - cantidadAnulada);
    }
}
