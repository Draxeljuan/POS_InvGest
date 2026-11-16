package com.invengest.transaction;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.exception.StockInsuficienteException;
import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.DetalleVenta;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.model.ServicioCatalogo;
import com.invengest.transaction.domain.model.TipoDocumento;
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
import com.invengest.transaction.domain.usecase.AnularVentaUseCase;
import com.invengest.transaction.domain.usecase.RegistrarVentaUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarYAnularVentaUseCaseTest {

    @Mock
    private VentaGateway ventaGateway;
    @Mock
    private CatalogGateway catalogGateway;
    @Mock
    private MovimientoInventarioGateway movimientoInventarioGateway;
    @Mock
    private ClienteGateway clienteGateway;
    @Mock
    private MetodoPagoGateway metodoPagoGateway;
    @Mock
    private TipoMovimientoGateway tipoMovimientoGateway;
    @Mock
    private AnalyticsGateway analyticsGateway;
    @Mock
    private NotificacionGateway notificacionGateway;

    private RegistrarVentaUseCase registrarVentaUseCase;
    private AnularVentaUseCase anularVentaUseCase;

    @BeforeEach
    void setUp() {
        registrarVentaUseCase = new RegistrarVentaUseCase(
                ventaGateway,
                catalogGateway,
                movimientoInventarioGateway,
                clienteGateway,
                metodoPagoGateway,
                tipoMovimientoGateway,
                analyticsGateway,
                notificacionGateway
        );
        anularVentaUseCase = new AnularVentaUseCase(
                ventaGateway,
                catalogGateway,
                movimientoInventarioGateway,
                tipoMovimientoGateway,
                analyticsGateway
        );
    }

    @Test
    void registrarVentaFlujoFelizValidaBatchDescuentaStockGuardaKardexYNotificaAislado() {
        // Arrange
        Cliente cliente = new Cliente(1, new TipoDocumento(1, "CC"), "1020", "Ana", null, "López", null, "310", "ana@correo.com");
        MetodoPago efectivo = new MetodoPago(1, "Efectivo");
        DetalleVenta itemProd = new DetalleVenta(null, null, "PROD-001", TipoItem.PRODUCTO, null, null, 2, null);
        DetalleVenta itemServ = new DetalleVenta(null, null, "5", TipoItem.SERVICIO, null, null, 1, null);

        Venta solicitud = new Venta();
        solicitud.setIdUsuario(10);
        solicitud.setCliente(new Cliente(1, null, null, null, null, null, null, null, null));
        solicitud.setMetodoPago(new MetodoPago(1, null));
        solicitud.setDetalles(List.of(itemProd, itemServ));

        when(metodoPagoGateway.findById(1)).thenReturn(Optional.of(efectivo));
        when(clienteGateway.findById(1)).thenReturn(Optional.of(cliente));
        when(catalogGateway.validarStockBatch(anyList())).thenReturn(List.of(
                new ValidacionStockItem("PROD-001", "Resma Carta", new BigDecimal("18000.00"), 15, 2, true)
        ));
        when(catalogGateway.obtenerServicioPorId(5)).thenReturn(Optional.of(
                new ServicioCatalogo(5, "Anillado", "Anillado plástico", new BigDecimal("4000.00"))
        ));
        when(tipoMovimientoGateway.findByNombre(TipoMovimiento.MOV_SALIDA_VENTA))
                .thenReturn(Optional.of(new TipoMovimiento(2, TipoMovimiento.MOV_SALIDA_VENTA)));
        when(ventaGateway.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(101);
            return v;
        });

        // Act
        Venta resultado = registrarVentaUseCase.registrarVenta(solicitud);

        // Assert
        assertEquals(101, resultado.getIdVenta());
        assertEquals(new BigDecimal("40000.00"), resultado.getTotal());
        assertEquals(Venta.ESTADO_COMPLETADA, resultado.getEstado());
        verify(catalogGateway, times(1)).validarStockBatch(anyList());
        verify(catalogGateway, times(1)).descontarStockBatch(anyList());
        verify(movimientoInventarioGateway, times(1)).save(any(MovimientoInventario.class));
        verify(analyticsGateway, times(1)).sincronizarVenta(resultado);
        verify(notificacionGateway, times(1)).enviarComprobanteVenta(eq(resultado), isNull());
    }

    @Test
    void registrarVentaSoloServiciosYValidaCasosBordeDeProductoServicioYCabecera() {
        // Arrange
        MetodoPago efectivo = new MetodoPago(1, "Efectivo");
        when(metodoPagoGateway.findById(1)).thenReturn(Optional.of(efectivo));
        when(ventaGateway.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(105);
            v.setDetalles(List.of());
            return v;
        });

        DetalleVenta servAlfanumerico = new DetalleVenta(
                null, null, "SRV-IMP", TipoItem.SERVICIO, "Impresión láser", new BigDecimal("1000.00"), 3, null);
        Venta ventaSoloServicio = new Venta();
        ventaSoloServicio.setIdUsuario(10);
        ventaSoloServicio.setMetodoPago(new MetodoPago(1, null));
        ventaSoloServicio.setDetalles(List.of(servAlfanumerico));

        // Act
        Venta res = registrarVentaUseCase.registrarVenta(ventaSoloServicio);

        // Assert
        assertEquals(new BigDecimal("3000.00"), res.getTotal());
        assertEquals(1, res.getDetalles().size());
        assertThrows(ReglaNegocioException.class, () -> registrarVentaUseCase.registrarVenta(null));

        // Servicio sin nombre lanza ReglaNegocioException
        DetalleVenta servSinNombre = new DetalleVenta(null, null, "SRV-X", TipoItem.SERVICIO, "", new BigDecimal("1000.00"), 1, null);
        Venta vServInvalido = new Venta();
        vServInvalido.setIdUsuario(10);
        vServInvalido.setMetodoPago(new MetodoPago(1, null));
        vServInvalido.setDetalles(List.of(servSinNombre));
        assertThrows(ReglaNegocioException.class, () -> registrarVentaUseCase.registrarVenta(vServInvalido));
    }

    @Test
    void registrarVentaLanzaStockInsuficienteYNoGuardaNiDescuentaCuandoNoHayDisponibilidad() {
        // Arrange
        Venta solicitud = new Venta();
        solicitud.setIdUsuario(10);
        solicitud.setMetodoPago(new MetodoPago(1, null));
        solicitud.setDetalles(List.of(
                new DetalleVenta(null, null, "PROD-001", TipoItem.PRODUCTO, null, null, 20, null)
        ));

        when(metodoPagoGateway.findById(1)).thenReturn(Optional.of(new MetodoPago(1, "Efectivo")));
        when(catalogGateway.validarStockBatch(anyList())).thenReturn(List.of(
                new ValidacionStockItem("PROD-001", "Resma Carta", new BigDecimal("18000.00"), 5, 20, false)
        ));

        // Act & Assert
        assertThrows(StockInsuficienteException.class, () -> registrarVentaUseCase.registrarVenta(solicitud));
        verify(ventaGateway, never()).save(any());
        verify(catalogGateway, never()).descontarStockBatch(anyList());
    }

    @Test
    void registrarVentaEjecutaCompensacionReintegrandoStockCuandoFallaKardexPosteriorAlDescuento() {
        // Arrange
        Venta solicitud = new Venta();
        solicitud.setIdUsuario(10);
        solicitud.setMetodoPago(new MetodoPago(1, null));
        solicitud.setDetalles(List.of(
                new DetalleVenta(null, null, "PROD-001", TipoItem.PRODUCTO, null, null, 3, null)
        ));

        when(metodoPagoGateway.findById(1)).thenReturn(Optional.of(new MetodoPago(1, "Efectivo")));
        when(catalogGateway.validarStockBatch(anyList())).thenReturn(List.of(
                new ValidacionStockItem("PROD-001", "Marcador", new BigDecimal("3000.00"), 10, 3, true)
        ));
        when(ventaGateway.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(202);
            return v;
        });
        when(tipoMovimientoGateway.findByNombre(TipoMovimiento.MOV_SALIDA_VENTA))
                .thenReturn(Optional.of(new TipoMovimiento(2, TipoMovimiento.MOV_SALIDA_VENTA)));
        doThrow(new RuntimeException("Error BD Kardex")).when(movimientoInventarioGateway).save(any());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> registrarVentaUseCase.registrarVenta(solicitud));
        verify(catalogGateway, times(1)).descontarStockBatch(anyList());
        verify(catalogGateway, times(1)).reintegrarStockBatch(anyList());
        verify(ventaGateway, times(2)).save(any(Venta.class));
    }

    @Test
    void anularVentaCambiaEstadoReintegraStockRegistraKardexYSincronizaAnalytics() {
        // Arrange
        DetalleVenta d1 = new DetalleVenta(1, 50, "PROD-001", TipoItem.PRODUCTO, "Cuaderno",
                new BigDecimal("10000.00"), 2, new BigDecimal("20000.00"));
        Venta ventaExistente = new Venta(
                50, 10, LocalDateTime.of(2026, 11, 16, 9, 0),
                null, new MetodoPago(1, "Efectivo"), new BigDecimal("20000.00"),
                Venta.ESTADO_COMPLETADA, null, null, null, List.of(d1)
        );

        when(ventaGateway.findById(50)).thenReturn(Optional.of(ventaExistente));
        when(tipoMovimientoGateway.findByNombre(TipoMovimiento.MOV_ANULACION_VENTA))
                .thenReturn(Optional.empty());
        when(tipoMovimientoGateway.findById(5))
                .thenReturn(Optional.empty());
        when(ventaGateway.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("Analytics caído")).when(analyticsGateway).sincronizarAnulacionVenta(any());

        // Act
        Venta anulada = anularVentaUseCase.anularVenta(50, 1, "Devolución cliente");

        // Assert
        assertTrue(anulada.estaAnulada());
        assertEquals(1, anulada.getIdUsuarioAnulacion());
        assertEquals("Devolución cliente", anulada.getMotivoAnulacion());
        verify(catalogGateway, times(1)).reintegrarStockBatch(anyList());
        verify(movimientoInventarioGateway, times(1)).save(any(MovimientoInventario.class));
        verify(analyticsGateway, times(1)).sincronizarAnulacionVenta(anulada);

        assertThrows(ReglaNegocioException.class, () -> anularVentaUseCase.anularVenta(null, 1, "Motivo"));
        assertThrows(ReglaNegocioException.class, () -> anularVentaUseCase.anularVenta(50, 1, "Segundo intento"));
        assertThrows(RecursoNoEncontradoException.class, () -> anularVentaUseCase.anularVenta(999, 1, "No existe"));
    }
}
