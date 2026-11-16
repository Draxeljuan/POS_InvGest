package com.invengest.transaction.domain.model;

import com.invengest.transaction.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {
    private Integer idMovimientoInventario;
    private String idProducto;
    private Integer idUsuario;
    private TipoMovimiento tipoMovimiento;
    private Integer cantidad;
    private LocalDateTime fechaMovimiento;
    private String observacion;

    public void validarDatosObligatorios() {
        if (idProducto == null || idProducto.isBlank()) {
            throw new ReglaNegocioException("El ID del producto es obligatorio");
        }
        if (idUsuario == null) {
            throw new ReglaNegocioException("El ID del usuario es obligatorio");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a 0");
        }
        if (tipoMovimiento == null
                || (tipoMovimiento.getIdMovimiento() == null
                && (tipoMovimiento.getNombre() == null || tipoMovimiento.getNombre().isBlank()))) {
            throw new ReglaNegocioException("El tipo de movimiento es obligatorio");
        }
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }
    }

    public static MovimientoInventario crearParaVenta(
            String idProducto,
            Integer idUsuario,
            TipoMovimiento tipoSalida,
            Integer cantidad,
            Integer idVenta,
            LocalDateTime fecha) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setIdProducto(idProducto);
        mov.setIdUsuario(idUsuario);
        mov.setTipoMovimiento(tipoSalida);
        mov.setCantidad(cantidad);
        mov.setFechaMovimiento(fecha != null ? fecha : LocalDateTime.now());
        mov.setObservacion("Venta #" + idVenta);
        return mov;
    }

    public static MovimientoInventario crearParaAnulacion(
            String idProducto,
            Integer idUsuarioAnulacion,
            TipoMovimiento tipoAnulacion,
            Integer cantidad,
            Integer idVenta,
            String motivo,
            LocalDateTime fecha) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setIdProducto(idProducto);
        mov.setIdUsuario(idUsuarioAnulacion);
        mov.setTipoMovimiento(tipoAnulacion);
        mov.setCantidad(cantidad);
        mov.setFechaMovimiento(fecha != null ? fecha : LocalDateTime.now());
        String obs = "Anulación Venta #" + idVenta + (motivo != null && !motivo.isBlank() ? ": " + motivo.trim() : "");
        mov.setObservacion(obs.length() > 255 ? obs.substring(0, 255) : obs);
        return mov;
    }
}
