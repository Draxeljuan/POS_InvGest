package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import com.invengest.catalog.domain.exception.StockInsuficienteException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    private String idProducto;
    private Categoria categoria;
    private String nombre;
    private BigDecimal precioVenta;
    private BigDecimal costoCompra;
    private LocalDate fechaIngreso;
    private Integer stock;
    private Integer stockMinimo;
    private String ubicacion;
    private EstadoProducto estado;

    public void validarInvariantes() {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre del producto es obligatorio.");
        }
        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El precio de venta no puede ser negativo.");
        }
        if (costoCompra == null || costoCompra.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El costo de compra no puede ser negativo.");
        }
        if (precioVenta.compareTo(costoCompra) < 0) {
            throw new ReglaNegocioException("El precio de venta debe ser mayor o igual al costo de compra.");
        }
        if (stock == null || stock < 0) {
            throw new ReglaNegocioException("El stock actual no puede ser negativo.");
        }
        if (stockMinimo == null || stockMinimo < 0) {
            throw new ReglaNegocioException("El stock mínimo no puede ser negativo.");
        }
        this.nombre = nombre.trim();
        if (fechaIngreso == null) {
            this.fechaIngreso = LocalDate.now();
        }
    }

    public boolean tieneStockSuficiente(int cantidad) {
        if (cantidad <= 0) {
            return false;
        }
        int disponible = stock != null ? stock : 0;
        return disponible >= cantidad && (estado == null || !estado.esDescontinuado());
    }

    public void descontarStock(int cantidad, EstadoProducto estadoAgotado) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad a descontar debe ser mayor a cero.");
        }
        if (estado != null && estado.esDescontinuado()) {
            throw new ReglaNegocioException("No se puede descontar stock de un producto descontinuado: " + nombre);
        }
        int disponible = stock != null ? stock : 0;
        if (disponible < cantidad) {
            throw new StockInsuficienteException("Stock insuficiente para el producto: " + nombre
                    + " (disponible: " + disponible + ", solicitado: " + cantidad + ")");
        }
        this.stock = disponible - cantidad;
        if (this.stock == 0 && estadoAgotado != null) {
            this.estado = estadoAgotado;
        }
    }

    public void reintegrarStock(int cantidad, EstadoProducto estadoActivo) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad a reintegrar debe ser mayor a cero.");
        }
        int disponible = stock != null ? stock : 0;
        this.stock = disponible + cantidad;
        if (this.stock > 0 && estadoActivo != null && (this.estado == null || this.estado.esAgotado())) {
            this.estado = estadoActivo;
        }
    }

    public boolean estaEnStockBajo() {
        int disponible = stock != null ? stock : 0;
        int minimo = stockMinimo != null ? stockMinimo : 0;
        return disponible <= minimo;
    }

    public boolean estaAgotado() {
        int disponible = stock != null ? stock : 0;
        return disponible <= 0 || (estado != null && estado.esAgotado());
    }

    public String obtenerEtiquetaAlertaStock() {
        return estaAgotado() ? EstadoProducto.ESTADO_AGOTADO : "Bajo Stock";
    }

    public void descontinuar(EstadoProducto estadoDescontinuado) {
        if (estadoDescontinuado == null) {
            throw new ReglaNegocioException("El estado 'Descontinuado' es requerido para descontinuar el producto.");
        }
        this.estado = estadoDescontinuado;
    }

    public BigDecimal calcularMargenUnitario(BigDecimal costoProveedor) {
        if (precioVenta == null || costoProveedor == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }
        return precioVenta.subtract(costoProveedor).setScale(2, RoundingMode.HALF_EVEN);
    }

    public BigDecimal calcularPorcentajeMargen(BigDecimal costoProveedor) {
        if (precioVenta == null || costoProveedor == null || precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }
        BigDecimal margen = precioVenta.subtract(costoProveedor);
        return margen.multiply(BigDecimal.valueOf(100))
                .divide(precioVenta, 2, RoundingMode.HALF_EVEN);
    }
}
