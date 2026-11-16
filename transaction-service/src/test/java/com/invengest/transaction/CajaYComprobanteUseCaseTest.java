package com.invengest.transaction;

import com.invengest.transaction.domain.exception.EntidadDuplicadaException;
import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.transaction.domain.model.CalendarioLaboral;
import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.ComprobanteVenta;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.ResumenCaja;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.CierreCajaGateway;
import com.invengest.transaction.domain.model.gateway.NotificacionGateway;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import com.invengest.transaction.domain.usecase.CajaUseCase;
import com.invengest.transaction.domain.usecase.ComprobanteVentaUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CajaYComprobanteUseCaseTest {

    @Mock
    private VentaGateway ventaGateway;
    @Mock
    private CierreCajaGateway cierreCajaGateway;
    @Mock
    private CalendarioLaboral calendarioLaboral;
    @Mock
    private NotificacionGateway notificacionGateway;

    private CajaUseCase cajaUseCase;
    private ComprobanteVentaUseCase comprobanteVentaUseCase;

    @BeforeEach
    void setUp() {
        cajaUseCase = new CajaUseCase(ventaGateway, cierreCajaGateway, calendarioLaboral, notificacionGateway);
        comprobanteVentaUseCase = new ComprobanteVentaUseCase(ventaGateway, notificacionGateway);
    }

    @Test
    void obtenerResumenYRegistrarCierreCajaEnDiaFestivoCalculaTotalesYNotifica() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        MetodoPago efectivo = new MetodoPago(1, "Efectivo");
        MetodoPago transferencia = new MetodoPago(2, "Transferencia");

        Venta v1 = new Venta(1, 1, fecha.atTime(10, 0), null, efectivo,
                new BigDecimal("25000.00"), Venta.ESTADO_COMPLETADA, null, null, null, List.of());
        Venta v2 = new Venta(2, 1, fecha.atTime(11, 0), null, transferencia,
                new BigDecimal("15000.00"), Venta.ESTADO_COMPLETADA, null, null, null, List.of());
        Venta v3Anulada = new Venta(3, 1, fecha.atTime(12, 0), null, efectivo,
                new BigDecimal("10000.00"), Venta.ESTADO_ANULADA, fecha.atTime(12, 30), 1, "Error", List.of());

        when(ventaGateway.findByFechaBetween(any(), any())).thenReturn(List.of(v1, v2, v3Anulada));
        when(cierreCajaGateway.existsByFechaCierre(fecha)).thenReturn(false);
        when(calendarioLaboral.esDiaHabil(fecha)).thenReturn(false);
        when(calendarioLaboral.siguienteDiaHabil(fecha)).thenReturn(LocalDate.of(2026, 11, 17));
        when(cierreCajaGateway.save(any(CierreCaja.class))).thenAnswer(inv -> {
            CierreCaja c = inv.getArgument(0);
            c.setIdCierre(7);
            return c;
        });
        when(cierreCajaGateway.findByFechaCierre(fecha)).thenReturn(Optional.of(new CierreCaja()));
        when(cierreCajaGateway.findAll()).thenReturn(List.of(new CierreCaja()));

        // Act
        ResumenCaja resumen = cajaUseCase.obtenerResumenCaja(fecha);
        CierreCaja cierre = cajaUseCase.registrarCierreCaja(fecha, 1, new BigDecimal("25000.00"), "Arqueo exacto");

        // Assert
        assertEquals(new BigDecimal("40000.00"), resumen.getTotalVentasCompletadas());
        assertEquals(2, resumen.getCantidadVentasCompletadas());
        assertEquals(new BigDecimal("10000.00"), resumen.getTotalVentasAnuladas());
        assertEquals(1, resumen.getCantidadVentasAnuladas());
        assertEquals(new BigDecimal("25000.00"), resumen.getEfectivoEsperado());
        assertEquals(2, resumen.getTotalesPorMetodoPago().size());

        assertEquals(7, cierre.getIdCierre());
        assertEquals(new BigDecimal("0.00"), cierre.getDiferencia());
        assertFalse(cierre.getEsDiaHabil());
        assertEquals(LocalDate.of(2026, 11, 17), cierre.getProximoDiaHabil());
        verify(notificacionGateway).enviarResumenCierreCaja(cierre);

        assertEquals(1, cajaUseCase.listarCierres().size());
        assertThrows(ReglaNegocioException.class, () -> cajaUseCase.obtenerCierrePorFecha(null));
        assertThrows(ReglaNegocioException.class, () -> cajaUseCase.registrarCierreCaja(fecha, null, BigDecimal.ZERO, ""));
        assertThrows(ReglaNegocioException.class, () -> cajaUseCase.registrarCierreCaja(fecha, 1, new BigDecimal("-1"), ""));
        cajaUseCase.obtenerCierrePorFecha(fecha);
    }

    @Test
    void registrarCierreCajaAplicaDegradacionControladaSiApiFestivosFallaYLanzaConflictSiDuplicado() {
        // Arrange
        LocalDate sabado = LocalDate.of(2026, 11, 14);
        when(ventaGateway.findByFechaBetween(any(), any())).thenReturn(List.of());
        when(cierreCajaGateway.existsByFechaCierre(sabado)).thenReturn(false, true);
        when(calendarioLaboral.esDiaHabil(sabado))
                .thenThrow(new ServicioExternoNoDisponibleException("API Festivos caída"));
        when(cierreCajaGateway.save(any(CierreCaja.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CierreCaja cierreDegradado = cajaUseCase.registrarCierreCaja(sabado, 1, BigDecimal.ZERO, "Sin ventas");

        // Assert
        assertTrue(cierreDegradado.getEsDiaHabil());
        assertEquals(LocalDate.of(2026, 11, 16), cierreDegradado.getProximoDiaHabil()); // salta domingo 15
        assertThrows(EntidadDuplicadaException.class,
                () -> cajaUseCase.registrarCierreCaja(sabado, 1, BigDecimal.ZERO, "Duplicado"));
    }

    @Test
    void comprobanteVentaUseCaseGeneraYEnviaComprobantePorCorreo() {
        // Arrange
        Cliente cliente = new Cliente(1, null, "1001", "Laura", null, "Gómez", null, "300", "laura@correo.com");
        Venta venta = new Venta(12, 1, LocalDateTime.now(), cliente, new MetodoPago(1, "Efectivo"),
                new BigDecimal("12000.00"), Venta.ESTADO_COMPLETADA, null, null, null, List.of());

        when(ventaGateway.findById(12)).thenReturn(Optional.of(venta));

        // Act
        ComprobanteVenta comp = comprobanteVentaUseCase.obtenerComprobante(12);
        ComprobanteVenta enviadoConEmailCliente = comprobanteVentaUseCase.enviarComprobantePorCorreo(12, null);
        ComprobanteVenta enviadoConOverride = comprobanteVentaUseCase.enviarComprobantePorCorreo(12, "alterno@correo.com");

        // Assert
        assertEquals("POS-000012", comp.getNumeroComprobante());
        assertEquals("laura@correo.com", enviadoConEmailCliente.getEmailCliente());
        assertEquals("alterno@correo.com", enviadoConOverride.getEmailCliente());
        verify(notificacionGateway).enviarComprobanteVenta(venta, "laura@correo.com");
        verify(notificacionGateway).enviarComprobanteVenta(venta, "alterno@correo.com");

        Venta ventaSinCorreo = new Venta(13, 1, LocalDateTime.now(), null, new MetodoPago(1, "Efectivo"),
                new BigDecimal("5000.00"), Venta.ESTADO_COMPLETADA, null, null, null, List.of());
        when(ventaGateway.findById(13)).thenReturn(Optional.of(ventaSinCorreo));
        assertThrows(ReglaNegocioException.class,
                () -> comprobanteVentaUseCase.enviarComprobantePorCorreo(13, null));
        assertThrows(ReglaNegocioException.class,
                () -> comprobanteVentaUseCase.obtenerComprobante(null));
        assertThrows(ReglaNegocioException.class,
                () -> comprobanteVentaUseCase.enviarComprobantePorCorreo(null, "a@b.com"));
        assertThrows(RecursoNoEncontradoException.class,
                () -> comprobanteVentaUseCase.obtenerComprobante(999));
    }
}
