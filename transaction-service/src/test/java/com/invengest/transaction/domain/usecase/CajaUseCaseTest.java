package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.EntidadDuplicadaException;
import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.transaction.domain.model.CalendarioLaboral;
import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.ResumenCaja;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.CierreCajaGateway;
import com.invengest.transaction.domain.model.gateway.NotificacionGateway;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CajaUseCaseTest {

    @Mock
    private VentaGateway ventaGateway;
    @Mock
    private CierreCajaGateway cierreCajaGateway;
    @Mock
    private CalendarioLaboral calendarioLaboral;
    @Mock
    private NotificacionGateway notificacionGateway;

    @InjectMocks
    private CajaUseCase cajaUseCase;

    @Test
    @DisplayName("Debe obtener resumen y registrar cierre de caja en día festivo calculando totales y notificando")
    void debeObtenerResumenYRegistrarCierreCaja() {
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
        CierreCaja consultado = cajaUseCase.obtenerCierrePorFecha(fecha);
        List<CierreCaja> lista = cajaUseCase.listarCierres();

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
        assertEquals(2, cierre.getDetalles().size());
        assertEquals(1, lista.size());
        verify(notificacionGateway).enviarResumenCierreCaja(cierre);
    }

    @Test
    @DisplayName("Debe aplicar degradación controlada si API festivos falla y validar errores de negocio")
    void debeAplicarDegradacionControladaYValidarErrores() {
        // Arrange
        LocalDate sabado = LocalDate.of(2026, 11, 14);
        when(ventaGateway.findByFechaBetween(any(), any())).thenReturn(null);
        when(cierreCajaGateway.existsByFechaCierre(sabado)).thenReturn(false, true);
        when(calendarioLaboral.esDiaHabil(sabado))
                .thenThrow(new ServicioExternoNoDisponibleException("API Festivos caída"));
        when(cierreCajaGateway.save(any(CierreCaja.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cierreCajaGateway.findByFechaCierre(sabado)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Notif down")).when(notificacionGateway).enviarResumenCierreCaja(any());

        // Act
        CierreCaja cierreDegradado = cajaUseCase.registrarCierreCaja(sabado, 1, BigDecimal.ZERO, "Sin ventas");

        // Assert
        assertTrue(cierreDegradado.getEsDiaHabil());
        assertEquals(LocalDate.of(2026, 11, 16), cierreDegradado.getProximoDiaHabil());
        assertThrows(EntidadDuplicadaException.class,
                () -> cajaUseCase.registrarCierreCaja(sabado, 1, BigDecimal.ZERO, "Duplicado"));
        assertThrows(ReglaNegocioException.class, () -> cajaUseCase.obtenerCierrePorFecha(null));
        assertThrows(RecursoNoEncontradoException.class, () -> cajaUseCase.obtenerCierrePorFecha(sabado));
        assertThrows(ReglaNegocioException.class, () -> cajaUseCase.registrarCierreCaja(sabado, null, BigDecimal.ZERO, ""));
        assertThrows(ReglaNegocioException.class, () -> cajaUseCase.registrarCierreCaja(sabado, 1, null, ""));
        assertThrows(ReglaNegocioException.class, () -> cajaUseCase.registrarCierreCaja(sabado, 1, new BigDecimal("-1"), ""));
    }
}
