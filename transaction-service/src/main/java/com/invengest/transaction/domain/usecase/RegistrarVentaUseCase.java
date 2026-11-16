package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.exception.StockInsuficienteException;
import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.DetalleVenta;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.model.ServicioCatalogo;
import com.invengest.transaction.domain.model.SolicitudStockItem;
import com.invengest.transaction.domain.model.TipoItem;
import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.ValidacionStockItem;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.AnalyticsGateway;
import com.invengest.transaction.domain.model.gateway.CatalogGateway;
import com.invengest.transaction.domain.model.gateway.ClienteGateway;
import com.invengest.transaction.domain.model.gateway.MetodoPagoGateway;
import com.invengest.transaction.domain.model.gateway.MovimientoInventarioGateway;
import com.invengest.transaction.domain.model.gateway.NotificacionGateway;
import com.invengest.transaction.domain.model.gateway.TipoMovimientoGateway;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RegistrarVentaUseCase {

    private static final int ID_TIPO_MOVIMIENTO_SALIDA_VENTA = 2;

    private final VentaGateway ventaGateway;
    private final CatalogGateway catalogGateway;
    private final MovimientoInventarioGateway movimientoInventarioGateway;
    private final ClienteGateway clienteGateway;
    private final MetodoPagoGateway metodoPagoGateway;
    private final TipoMovimientoGateway tipoMovimientoGateway;
    private final AnalyticsGateway analyticsGateway;
    private final NotificacionGateway notificacionGateway;

    public Venta registrarVenta(Venta venta) {
        if (venta == null) {
            throw new ReglaNegocioException("Los datos de la venta no pueden ser nulos");
        }
        venta.validarCabecera();
        validarClienteYMetodoPago(venta);

        List<DetalleVenta> detalles = venta.getDetalles();
        for (DetalleVenta detalle : detalles) {
            detalle.validarEstructura();
        }

        List<SolicitudStockItem> solicitudesProductos = agruparSolicitudesProductos(detalles);
        Map<String, ValidacionStockItem> productosValidados = validarDisponibilidadBatch(solicitudesProductos);

        List<DetalleVenta> detallesProcesados = new ArrayList<>();
        for (DetalleVenta detalle : detalles) {
            if (detalle.getTipoItem() == TipoItem.PRODUCTO) {
                procesarDetalleProducto(detalle, productosValidados);
            } else if (detalle.getTipoItem() == TipoItem.SERVICIO) {
                procesarDetalleServicio(detalle);
            }
            detallesProcesados.add(detalle);
        }

        venta.setDetalles(detallesProcesados);
        venta.calcularTotal();
        venta.marcarCompletada(LocalDateTime.now());

        Venta ventaGuardada = ventaGateway.save(venta);
        if (ventaGuardada.getDetalles() == null || ventaGuardada.getDetalles().isEmpty()) {
            ventaGuardada.setDetalles(detallesProcesados);
        }

        if (!solicitudesProductos.isEmpty()) {
            catalogGateway.descontarStockBatch(solicitudesProductos);
            try {
                registrarSalidasKardex(ventaGuardada, detallesProcesados);
            } catch (RuntimeException ex) {
                ejecutarCompensacionStock(solicitudesProductos, ventaGuardada);
                throw ex;
            }
        }

        notificarEfectosSecundariosAislados(ventaGuardada);

        return ventaGuardada;
    }

    private void validarClienteYMetodoPago(Venta venta) {
        Integer idMetodoPago = venta.getMetodoPago().getIdMetodoPago();
        MetodoPago metodoPago = metodoPagoGateway.findById(idMetodoPago)
                .orElseThrow(() -> new RecursoNoEncontradoException("Método de pago no encontrado con ID: " + idMetodoPago));
        venta.setMetodoPago(metodoPago);

        if (venta.getCliente() != null && venta.getCliente().getIdCliente() != null) {
            Integer idCliente = venta.getCliente().getIdCliente();
            Cliente cliente = clienteGateway.findById(idCliente)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con ID: " + idCliente));
            venta.setCliente(cliente);
        }
    }

    private List<SolicitudStockItem> agruparSolicitudesProductos(List<DetalleVenta> detalles) {
        Map<String, Integer> acumuladoPorProducto = new LinkedHashMap<>();
        for (DetalleVenta detalle : detalles) {
            if (detalle.getTipoItem() == TipoItem.PRODUCTO) {
                acumuladoPorProducto.merge(detalle.getIdItem().trim(), detalle.getCantidad(), Integer::sum);
            }
        }
        return acumuladoPorProducto.entrySet().stream()
                .map(entry -> new SolicitudStockItem(entry.getKey(), entry.getValue()))
                .toList();
    }

    private Map<String, ValidacionStockItem> validarDisponibilidadBatch(List<SolicitudStockItem> solicitudesProductos) {
        if (solicitudesProductos.isEmpty()) {
            return Map.of();
        }
        List<ValidacionStockItem> validaciones = catalogGateway.validarStockBatch(solicitudesProductos);
        if (validaciones == null || validaciones.isEmpty()) {
            throw new RecursoNoEncontradoException("No se obtuvo validación de stock desde el catálogo");
        }
        Map<String, ValidacionStockItem> mapa = validaciones.stream()
                .collect(Collectors.toMap(
                        ValidacionStockItem::getIdProducto,
                        Function.identity(),
                        (a, b) -> a
                ));

        for (SolicitudStockItem solicitud : solicitudesProductos) {
            ValidacionStockItem validado = mapa.get(solicitud.getIdProducto());
            if (validado == null) {
                throw new RecursoNoEncontradoException(
                        "Producto no encontrado en catálogo con código: " + solicitud.getIdProducto());
            }
            int disponible = validado.getStockDisponible() != null ? validado.getStockDisponible() : 0;
            if (!validado.isSuficiente() || disponible < solicitud.getCantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para el producto: " + validado.getNombre()
                                + " (Disponible: " + disponible
                                + ", Solicitado: " + solicitud.getCantidad() + ")");
            }
        }
        return mapa;
    }

    private void procesarDetalleProducto(DetalleVenta detalle, Map<String, ValidacionStockItem> productosValidados) {
        ValidacionStockItem validado = productosValidados.get(detalle.getIdItem().trim());
        detalle.setIdItem(validado.getIdProducto());
        detalle.setNombreItem(validado.getNombre());

        BigDecimal precioUnitario = detalle.getPrecioUnitario() != null
                ? detalle.getPrecioUnitario()
                : validado.getPrecioVenta();
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El precio unitario del producto no puede ser nulo o negativo");
        }
        detalle.setPrecioUnitario(precioUnitario);
        detalle.calcularSubtotal();
    }

    private void procesarDetalleServicio(DetalleVenta detalle) {
        Integer idServicio = null;
        try {
            idServicio = Integer.valueOf(detalle.getIdItem().trim());
        } catch (NumberFormatException ignored) {
            // El identificador del servicio puede ser alfanumérico
        }

        if (idServicio != null) {
            ServicioCatalogo servicio = catalogGateway.obtenerServicioPorId(idServicio).orElse(null);
            if (servicio != null) {
                if (detalle.getNombreItem() == null || detalle.getNombreItem().isBlank()) {
                    detalle.setNombreItem(servicio.getNombre());
                }
                if (detalle.getPrecioUnitario() == null) {
                    detalle.setPrecioUnitario(servicio.getPrecioSugerido());
                }
            }
        }

        if (detalle.getNombreItem() == null || detalle.getNombreItem().isBlank()) {
            throw new ReglaNegocioException("El nombre del servicio es obligatorio para el ítem: " + detalle.getIdItem());
        }
        if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El precio unitario del servicio no puede ser nulo o negativo");
        }
        detalle.calcularSubtotal();
    }

    private void registrarSalidasKardex(Venta ventaGuardada, List<DetalleVenta> detalles) {
        TipoMovimiento tipoSalida = obtenerTipoMovimientoSalida();
        LocalDateTime fechaMov = ventaGuardada.getFecha() != null ? ventaGuardada.getFecha() : LocalDateTime.now();

        for (DetalleVenta detalle : detalles) {
            if (detalle.esProducto()) {
                MovimientoInventario movimiento = MovimientoInventario.crearParaVenta(
                        detalle.getIdItem(),
                        ventaGuardada.getIdUsuario(),
                        tipoSalida,
                        detalle.getCantidad(),
                        ventaGuardada.getIdVenta(),
                        fechaMov
                );
                movimientoInventarioGateway.save(movimiento);
            }
        }
    }

    private void ejecutarCompensacionStock(List<SolicitudStockItem> solicitudesProductos, Venta ventaGuardada) {
        try {
            catalogGateway.reintegrarStockBatch(solicitudesProductos);
        } catch (RuntimeException ignored) {
            // La excepción original prevalece; el adaptador Feign registra el fallo de compensación
        }
        try {
            if (ventaGuardada != null && ventaGuardada.getIdVenta() != null && !ventaGuardada.estaAnulada()) {
                ventaGuardada.anular(
                        ventaGuardada.getIdUsuario(),
                        "Compensación automática por fallo posterior al descuento de stock",
                        LocalDateTime.now()
                );
                ventaGateway.save(ventaGuardada);
            }
        } catch (RuntimeException ignored) {
            // No ocultar la excepción principal
        }
    }

    private void notificarEfectosSecundariosAislados(Venta ventaGuardada) {
        try {
            analyticsGateway.sincronizarVenta(ventaGuardada);
        } catch (RuntimeException ignored) {
            // Aislado para no afectar la transacción de venta
        }
        try {
            if (ventaGuardada.getCliente() != null && ventaGuardada.getCliente().tieneEmailValido()) {
                notificacionGateway.enviarComprobanteVenta(ventaGuardada, null);
            }
        } catch (RuntimeException ignored) {
            // Aislado para no afectar la transacción de venta
        }
    }

    private TipoMovimiento obtenerTipoMovimientoSalida() {
        return tipoMovimientoGateway.findByNombre(TipoMovimiento.MOV_SALIDA_VENTA)
                .or(() -> tipoMovimientoGateway.findById(ID_TIPO_MOVIMIENTO_SALIDA_VENTA))
                .orElseGet(() -> new TipoMovimiento(ID_TIPO_MOVIMIENTO_SALIDA_VENTA, TipoMovimiento.MOV_SALIDA_VENTA));
    }
}
