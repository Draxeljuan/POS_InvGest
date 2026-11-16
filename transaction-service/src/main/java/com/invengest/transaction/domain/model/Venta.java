package com.invengest.transaction.domain.model;

import com.invengest.transaction.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    public static final String ESTADO_COMPLETADA = "COMPLETADA";
    public static final String ESTADO_ANULADA = "ANULADA";

    private Integer idVenta;
    private Integer idUsuario;
    private LocalDateTime fecha;
    private Cliente cliente;
    private MetodoPago metodoPago;
    private BigDecimal total;
    private String estado = ESTADO_COMPLETADA;
    private LocalDateTime fechaAnulacion;
    private Integer idUsuarioAnulacion;
    private String motivoAnulacion;
    private List<DetalleVenta> detalles = new ArrayList<>();

    public void validarCabecera() {
        if (idUsuario == null) {
            throw new ReglaNegocioException("El ID del usuario vendedor es obligatorio");
        }
        if (metodoPago == null || metodoPago.getIdMetodoPago() == null) {
            throw new ReglaNegocioException("El método de pago es obligatorio");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new ReglaNegocioException("La venta debe contener al menos un ítem");
        }
    }

    public BigDecimal calcularTotal() {
        if (detalles == null || detalles.isEmpty()) {
            this.total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return this.total;
        }
        BigDecimal acumulado = BigDecimal.ZERO;
        for (DetalleVenta detalle : detalles) {
            BigDecimal sub = detalle.getSubtotal() != null ? detalle.getSubtotal() : detalle.calcularSubtotal();
            acumulado = acumulado.add(sub);
        }
        this.total = acumulado.setScale(2, RoundingMode.HALF_UP);
        return this.total;
    }

    public void marcarCompletada(LocalDateTime instante) {
        this.estado = ESTADO_COMPLETADA;
        if (this.fecha == null) {
            this.fecha = instante != null ? instante : LocalDateTime.now();
        }
    }

    public void anular(Integer idUsuarioAdmin, String motivo, LocalDateTime instanteAnulacion) {
        if (estaAnulada()) {
            throw new ReglaNegocioException("La venta #" + idVenta + " ya se encuentra ANULADA");
        }
        if (idUsuarioAdmin == null) {
            throw new ReglaNegocioException("El ID del usuario que anula la venta es obligatorio");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("El motivo de anulación es obligatorio");
        }
        this.estado = ESTADO_ANULADA;
        this.idUsuarioAnulacion = idUsuarioAdmin;
        this.motivoAnulacion = motivo.trim();
        this.fechaAnulacion = instanteAnulacion != null ? instanteAnulacion : LocalDateTime.now();
    }

    public boolean estaAnulada() {
        return ESTADO_ANULADA.equalsIgnoreCase(this.estado);
    }

    public boolean estaCompletada() {
        return estado == null || ESTADO_COMPLETADA.equalsIgnoreCase(this.estado);
    }

    public List<DetalleVenta> obtenerDetallesProducto() {
        if (detalles == null) {
            return List.of();
        }
        return detalles.stream()
                .filter(DetalleVenta::esProducto)
                .toList();
    }
}
