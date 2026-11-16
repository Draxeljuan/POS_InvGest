package com.invengest.analytics.domain.usecase;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.ItemVentaSincronizacion;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.domain.model.gateway.AlertaStockGateway;
import com.invengest.analytics.domain.model.gateway.ResumenVentaGateway;
import com.invengest.analytics.domain.model.gateway.TopProductoGateway;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SincronizarAnaliticaUseCase {

    private final ResumenVentaGateway resumenVentaGateway;
    private final TopProductoGateway topProductoGateway;
    private final AlertaStockGateway alertaStockGateway;

    public SincronizarAnaliticaUseCase(
            ResumenVentaGateway resumenVentaGateway,
            TopProductoGateway topProductoGateway,
            AlertaStockGateway alertaStockGateway) {
        this.resumenVentaGateway = resumenVentaGateway;
        this.topProductoGateway = topProductoGateway;
        this.alertaStockGateway = alertaStockGateway;
    }

    public ResumenVentasDiarias registrarVentaSincronizada(
            LocalDate fecha,
            BigDecimal totalVenta,
            List<ItemVentaSincronizacion> items) {
        if (totalVenta == null || totalVenta.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El monto total de la venta debe ser mayor o igual a cero.");
        }

        LocalDate fechaOperacion = fecha != null ? fecha : LocalDate.now();

        ResumenVentasDiarias resumen = resumenVentaGateway.findByFecha(fechaOperacion)
                .orElseGet(() -> ResumenVentasDiarias.crearVacio(fechaOperacion));

        resumen.registrarVenta(totalVenta);
        ResumenVentasDiarias resumenGuardado = resumenVentaGateway.save(resumen);

        if (items != null) {
            for (ItemVentaSincronizacion item : items) {
                if (item != null
                        && item.getIdProducto() != null && !item.getIdProducto().isBlank()
                        && item.getCantidadVendida() != null && item.getCantidadVendida() > 0) {
                    TopProducto topProducto = topProductoGateway
                            .findByIdProductoAndFecha(item.getIdProducto(), fechaOperacion)
                            .orElseGet(() -> TopProducto.crearInicial(
                                    item.getIdProducto(),
                                    item.getNombreProducto(),
                                    fechaOperacion
                            ));

                    topProducto.incrementarCantidad(item.getCantidadVendida(), item.getNombreProducto());
                    topProductoGateway.save(topProducto);
                }
            }
        }

        return resumenGuardado;
    }

    public ResumenVentasDiarias registrarAnulacionSincronizada(
            LocalDate fechaVenta,
            BigDecimal totalAnulado,
            List<ItemVentaSincronizacion> items) {
        if (totalAnulado == null || totalAnulado.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El monto anulado debe ser mayor o igual a cero.");
        }

        LocalDate fechaOperacion = fechaVenta != null ? fechaVenta : LocalDate.now();

        ResumenVentasDiarias resumen = resumenVentaGateway.findByFecha(fechaOperacion)
                .orElseGet(() -> ResumenVentasDiarias.crearVacio(fechaOperacion));

        resumen.registrarAnulacion(totalAnulado);
        ResumenVentasDiarias resumenGuardado = resumenVentaGateway.save(resumen);

        if (items != null) {
            for (ItemVentaSincronizacion item : items) {
                if (item != null
                        && item.getIdProducto() != null && !item.getIdProducto().isBlank()
                        && item.getCantidadVendida() != null && item.getCantidadVendida() > 0) {
                    Optional<TopProducto> existenteOpt = topProductoGateway
                            .findByIdProductoAndFecha(item.getIdProducto(), fechaOperacion);

                    if (existenteOpt.isPresent()) {
                        TopProducto topProducto = existenteOpt.get();
                        topProducto.descontarAnulacion(item.getCantidadVendida());
                        topProductoGateway.save(topProducto);
                    }
                }
            }
        }

        return resumenGuardado;
    }

    public Optional<AlertaStock> sincronizarAlertaStock(
            String idProducto,
            String nombreProducto,
            Short stockActual,
            Short stockMinimo) {
        if (idProducto == null || idProducto.isBlank()) {
            throw new ReglaNegocioException("El identificador del producto es obligatorio.");
        }
        if (nombreProducto == null || nombreProducto.isBlank()) {
            throw new ReglaNegocioException("El nombre del producto es obligatorio.");
        }
        if (stockActual == null) {
            throw new ReglaNegocioException("El stock actual del producto es obligatorio.");
        }

        if (!AlertaStock.requiereAlerta(stockActual, stockMinimo)) {
            alertaStockGateway.deleteByIdProducto(idProducto.trim());
            return Optional.empty();
        }

        AlertaStock alerta = alertaStockGateway.findByIdProducto(idProducto.trim())
                .orElseGet(() -> AlertaStock.builder()
                        .idProducto(idProducto.trim())
                        .build());

        alerta.actualizarDatosAlerta(nombreProducto, stockActual, stockMinimo);

        return Optional.of(alertaStockGateway.save(alerta));
    }
}
