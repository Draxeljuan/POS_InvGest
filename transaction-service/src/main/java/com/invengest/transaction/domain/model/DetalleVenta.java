package com.invengest.transaction.domain.model;

import com.invengest.transaction.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {
    private Integer idDetalleVenta;
    private Integer idVenta;
    private String idItem;
    private TipoItem tipoItem;
    private String nombreItem;
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private BigDecimal subtotal;

    public void validarEstructura() {
        if (idItem == null || idItem.isBlank()) {
            throw new ReglaNegocioException("El identificador del ítem es obligatorio");
        }
        if (tipoItem == null) {
            throw new ReglaNegocioException("El tipo de ítem (PRODUCTO o SERVICIO) es obligatorio");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a 0 para el ítem: " + idItem);
        }
    }

    public BigDecimal calcularSubtotal() {
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El precio unitario no puede ser nulo o negativo para el ítem: " + idItem);
        }
        if (cantidad == null || cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a 0 para el ítem: " + idItem);
        }
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
        return this.subtotal;
    }

    public boolean esProducto() {
        return tipoItem == TipoItem.PRODUCTO;
    }

    public boolean esServicio() {
        return tipoItem == TipoItem.SERVICIO;
    }
}
