package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.DetalleVenta;
import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.model.SolicitudStockItem;
import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.AnalyticsGateway;
import com.invengest.transaction.domain.model.gateway.CatalogGateway;
import com.invengest.transaction.domain.model.gateway.MovimientoInventarioGateway;
import com.invengest.transaction.domain.model.gateway.TipoMovimientoGateway;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AnularVentaUseCase {

    private static final int ID_TIPO_MOVIMIENTO_ANULACION_VENTA = 5;

    private final VentaGateway ventaGateway;
    private final CatalogGateway catalogGateway;
    private final MovimientoInventarioGateway movimientoInventarioGateway;
    private final TipoMovimientoGateway tipoMovimientoGateway;
    private final AnalyticsGateway analyticsGateway;

    public Venta anularVenta(Integer idVenta, Integer idUsuarioAnulacion, String motivoAnulacion) {
        if (idVenta == null) {
            throw new ReglaNegocioException("El ID de la venta a anular es obligatorio");
        }
        Venta venta = ventaGateway.findById(idVenta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con ID: " + idVenta));

        LocalDateTime instanteAnulacion = LocalDateTime.now();
        venta.anular(idUsuarioAnulacion, motivoAnulacion, instanteAnulacion);

        List<DetalleVenta> detallesProducto = venta.obtenerDetallesProducto();
        if (!detallesProducto.isEmpty()) {
            List<SolicitudStockItem> solicitudesReintegro = agruparSolicitudesReintegro(detallesProducto);
            catalogGateway.reintegrarStockBatch(solicitudesReintegro);

            TipoMovimiento tipoAnulacion = obtenerTipoMovimientoAnulacion();
            for (DetalleVenta detalle : detallesProducto) {
                MovimientoInventario mov = MovimientoInventario.crearParaAnulacion(
                        detalle.getIdItem(),
                        idUsuarioAnulacion,
                        tipoAnulacion,
                        detalle.getCantidad(),
                        venta.getIdVenta(),
                        venta.getMotivoAnulacion(),
                        instanteAnulacion
                );
                movimientoInventarioGateway.save(mov);
            }
        }

        Venta ventaAnulada = ventaGateway.save(venta);
        if (ventaAnulada.getDetalles() == null || ventaAnulada.getDetalles().isEmpty()) {
            ventaAnulada.setDetalles(venta.getDetalles());
        }

        try {
            analyticsGateway.sincronizarAnulacionVenta(ventaAnulada);
        } catch (RuntimeException ignored) {
            // Aislado para no interrumpir la anulación
        }

        return ventaAnulada;
    }

    private List<SolicitudStockItem> agruparSolicitudesReintegro(List<DetalleVenta> detallesProducto) {
        Map<String, Integer> acumulado = new LinkedHashMap<>();
        for (DetalleVenta detalle : detallesProducto) {
            acumulado.merge(detalle.getIdItem().trim(), detalle.getCantidad(), Integer::sum);
        }
        return acumulado.entrySet().stream()
                .map(e -> new SolicitudStockItem(e.getKey(), e.getValue()))
                .toList();
    }

    private TipoMovimiento obtenerTipoMovimientoAnulacion() {
        return tipoMovimientoGateway.findByNombre(TipoMovimiento.MOV_ANULACION_VENTA)
                .or(() -> tipoMovimientoGateway.findById(ID_TIPO_MOVIMIENTO_ANULACION_VENTA))
                .orElseGet(() -> new TipoMovimiento(ID_TIPO_MOVIMIENTO_ANULACION_VENTA, TipoMovimiento.MOV_ANULACION_VENTA));
    }
}
