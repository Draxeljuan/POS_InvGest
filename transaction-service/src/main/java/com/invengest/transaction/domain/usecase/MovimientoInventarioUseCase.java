package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.model.SolicitudStockItem;
import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.gateway.CatalogGateway;
import com.invengest.transaction.domain.model.gateway.MovimientoInventarioGateway;
import com.invengest.transaction.domain.model.gateway.TipoMovimientoGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class MovimientoInventarioUseCase {

    private final MovimientoInventarioGateway movimientoInventarioGateway;
    private final TipoMovimientoGateway tipoMovimientoGateway;
    private final CatalogGateway catalogGateway;

    public MovimientoInventario registrarAjuste(MovimientoInventario movimiento) {
        if (movimiento == null) {
            throw new ReglaNegocioException("El movimiento de inventario no puede ser nulo");
        }
        movimiento.validarDatosObligatorios();

        TipoMovimiento tipoMovimiento = resolverTipoMovimiento(movimiento.getTipoMovimiento());
        if (!tipoMovimiento.esAjusteManualPermitido()) {
            throw new ReglaNegocioException(
                    "El tipo de movimiento '" + tipoMovimiento.getNombre()
                            + "' pertenece al flujo de ventas POS y no puede registrarse como ajuste manual");
        }
        movimiento.setTipoMovimiento(tipoMovimiento);

        List<SolicitudStockItem> solicitud = List.of(
                new SolicitudStockItem(movimiento.getIdProducto().trim(), movimiento.getCantidad())
        );

        boolean descontoStock = false;
        boolean reintegroStock = false;

        if (tipoMovimiento.esDecrementoStock()) {
            catalogGateway.descontarStockBatch(solicitud);
            descontoStock = true;
        } else if (tipoMovimiento.esIncrementoStock()) {
            catalogGateway.reintegrarStockBatch(solicitud);
            reintegroStock = true;
        } else {
            throw new ReglaNegocioException(
                    "El tipo de movimiento '" + tipoMovimiento.getNombre() + "' no define operación válida sobre stock");
        }

        try {
            return movimientoInventarioGateway.save(movimiento);
        } catch (RuntimeException ex) {
            compensarSincronizacionCatalogo(solicitud, descontoStock, reintegroStock);
            throw ex;
        }
    }

    public MovimientoInventario registrarMovimiento(MovimientoInventario movimiento) {
        return registrarAjuste(movimiento);
    }

    public List<MovimientoInventario> obtenerKardexPorProducto(String idProducto) {
        if (idProducto == null || idProducto.isBlank()) {
            throw new ReglaNegocioException("El ID del producto es obligatorio para consultar el Kardex");
        }
        return movimientoInventarioGateway.findByIdProducto(idProducto.trim());
    }

    public List<MovimientoInventario> listarTodos() {
        return movimientoInventarioGateway.findAll();
    }

    public List<MovimientoInventario> filtrarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new ReglaNegocioException("Las fechas inicial y final son obligatorias");
        }
        if (fin.isBefore(inicio)) {
            throw new ReglaNegocioException("La fecha final no puede ser anterior a la fecha inicial");
        }
        return movimientoInventarioGateway.findByFechaBetween(inicio, fin);
    }

    private TipoMovimiento resolverTipoMovimiento(TipoMovimiento entrada) {
        if (entrada.getIdMovimiento() != null) {
            return tipoMovimientoGateway.findById(entrada.getIdMovimiento())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Tipo de movimiento no encontrado con ID: " + entrada.getIdMovimiento()));
        }
        return tipoMovimientoGateway.findByNombre(entrada.getNombre().trim())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Tipo de movimiento no encontrado con nombre: " + entrada.getNombre()));
    }

    private void compensarSincronizacionCatalogo(
            List<SolicitudStockItem> solicitud,
            boolean descontoStock,
            boolean reintegroStock) {
        try {
            if (descontoStock) {
                catalogGateway.reintegrarStockBatch(solicitud);
            } else if (reintegroStock) {
                catalogGateway.descontarStockBatch(solicitud);
            }
        } catch (RuntimeException ignored) {
            // La excepción principal prevalece
        }
    }
}
