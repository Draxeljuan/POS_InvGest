package com.invengest.transaction;

import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.CalendarioLaboral;
import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.ComprobanteVenta;
import com.invengest.transaction.domain.model.DetalleVenta;
import com.invengest.transaction.domain.model.DiaFestivo;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.domain.model.TipoItem;
import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.CalendarioFestivosGateway;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DomainModelsAndCalendarioLaboralTest {

    @Test
    void ventaCalculaTotalAnulaCorrectamenteEImpideDobleAnulacionYValidaRamas() {
        // Arrange
        DetalleVenta d1 = new DetalleVenta(1, null, "PROD-01", TipoItem.PRODUCTO, "Cuaderno",
                new BigDecimal("8500.00"), 2, null);
        DetalleVenta d2 = new DetalleVenta(2, null, "10", TipoItem.SERVICIO, "Fotocopia",
                new BigDecimal("500.00"), 4, new BigDecimal("2000.00"));
        Venta venta = new Venta();
        venta.setIdVenta(15);
        venta.setIdUsuario(1);
        venta.setCliente(new Cliente(1, new TipoDocumento(1, "CC"), "1001", "Juan", "Carlos", "Pérez", "Gómez", "300", "juan@test.com"));
        venta.setMetodoPago(new MetodoPago(1, "Efectivo"));
        venta.setDetalles(List.of(d1, d2));

        // Act
        venta.validarCabecera();
        BigDecimal total = venta.calcularTotal();
        venta.marcarCompletada(null);
        ComprobanteVenta comprobante = ComprobanteVenta.desdeVenta(venta);
        venta.anular(99, "Error en digitación", null);

        // Assert
        assertEquals(new BigDecimal("19000.00"), total);
        assertEquals("POS-000015", comprobante.getNumeroComprobante());
        assertEquals("Juan Carlos Pérez Gómez", comprobante.getNombreCliente());
        assertTrue(venta.estaAnulada());
        assertFalse(venta.estaCompletada());
        assertEquals(1, venta.obtenerDetallesProducto().size());
        assertTrue(d1.esProducto());
        assertFalse(d1.esServicio());
        assertTrue(d2.esServicio());
        assertFalse(d2.esProducto());

        // Ramas adicionales de Venta y ComprobanteVenta
        Venta ventaVacia = new Venta();
        ventaVacia.setDetalles(null);
        assertEquals(new BigDecimal("0.00"), ventaVacia.calcularTotal());
        assertEquals(0, ventaVacia.obtenerDetallesProducto().size());
        ComprobanteVenta compAnonimo = ComprobanteVenta.desdeVenta(ventaVacia);
        assertEquals("POS-000000", compAnonimo.getNumeroComprobante());
        assertEquals("Consumidor Final", compAnonimo.getNombreCliente());

        assertThrows(ReglaNegocioException.class, ventaVacia::validarCabecera);
        ventaVacia.setIdUsuario(1);
        assertThrows(ReglaNegocioException.class, ventaVacia::validarCabecera);
        ventaVacia.setMetodoPago(new MetodoPago(1, "Efectivo"));
        assertThrows(ReglaNegocioException.class, ventaVacia::validarCabecera);

        Venta ventaSinAnular = new Venta();
        assertThrows(ReglaNegocioException.class, () -> ventaSinAnular.anular(null, "Motivo", null));
        assertThrows(ReglaNegocioException.class, () -> ventaSinAnular.anular(1, "  ", null));
        assertThrows(ReglaNegocioException.class, () -> venta.anular(99, "Segundo intento", LocalDateTime.now()));
    }

    @Test
    void detalleVentaMovimientoInventarioCierreCajaClienteYTipoMovimientoValidanRamas() {
        // Arrange
        CierreCaja cierre = new CierreCaja();
        cierre.setEfectivoEsperado(null);
        cierre.setEfectivoContado(new BigDecimal("0.00"));

        TipoMovimiento movIngreso = new TipoMovimiento(1, TipoMovimiento.MOV_INGRESO_COMPRA);
        TipoMovimiento movPositivo = new TipoMovimiento(6, TipoMovimiento.MOV_AJUSTE_POSITIVO_CONTEO);
        TipoMovimiento movAnulacion = new TipoMovimiento(5, TipoMovimiento.MOV_ANULACION_VENTA);
        TipoMovimiento movPerdida = new TipoMovimiento(3, TipoMovimiento.MOV_AJUSTE_PERDIDA_DANO);
        TipoMovimiento movConteo = new TipoMovimiento(4, TipoMovimiento.MOV_AJUSTE_CONTEO_FISICO);
        TipoMovimiento movSalida = new TipoMovimiento(2, TipoMovimiento.MOV_SALIDA_VENTA);
        TipoMovimiento movNulo = new TipoMovimiento(9, null);

        // Act & Assert
        assertEquals(new BigDecimal("0.00"), cierre.calcularDiferencia());
        assertFalse(cierre.tieneDescuadre());
        assertTrue(movIngreso.esIncrementoStock());
        assertTrue(movPositivo.esIncrementoStock());
        assertTrue(movAnulacion.esIncrementoStock());
        assertFalse(movNulo.esIncrementoStock());
        assertTrue(movPerdida.esDecrementoStock());
        assertTrue(movConteo.esDecrementoStock());
        assertTrue(movSalida.esDecrementoStock());
        assertFalse(movNulo.esDecrementoStock());
        assertFalse(movNulo.esAjusteManualPermitido());
        assertFalse(movAnulacion.esAjusteManualPermitido());

        Cliente cliSinEmail = new Cliente(1, null, "1", "Ana", "", "Gil", "", "300", "invalido");
        assertEquals("Ana Gil", cliSinEmail.getNombreCompleto());
        assertFalse(cliSinEmail.tieneEmailValido());
        assertFalse(new MetodoPago(2, "Transferencia").esEfectivo());
        assertFalse(new MetodoPago(3, null).esEfectivo());

        // Ramas de DetalleVenta
        DetalleVenta dSinTipo = new DetalleVenta(null, null, "P1", null, "X", BigDecimal.TEN, 1, null);
        assertThrows(ReglaNegocioException.class, dSinTipo::validarEstructura);
        DetalleVenta dCantCero = new DetalleVenta(null, null, "P1", TipoItem.PRODUCTO, "X", BigDecimal.TEN, 0, null);
        assertThrows(ReglaNegocioException.class, dCantCero::validarEstructura);
        assertThrows(ReglaNegocioException.class, dCantCero::calcularSubtotal);
        DetalleVenta dPrecioNeg = new DetalleVenta(null, null, "P1", TipoItem.PRODUCTO, "X", new BigDecimal("-1"), 1, null);
        assertThrows(ReglaNegocioException.class, dPrecioNeg::calcularSubtotal);

        // Ramas de MovimientoInventario
        MovimientoInventario m1 = new MovimientoInventario(null, "P1", null, movIngreso, 1, null, null);
        assertThrows(ReglaNegocioException.class, m1::validarDatosObligatorios);
        MovimientoInventario m2 = new MovimientoInventario(null, "P1", 1, movIngreso, 0, null, null);
        assertThrows(ReglaNegocioException.class, m2::validarDatosObligatorios);
        MovimientoInventario m3 = new MovimientoInventario(null, "P1", 1, new TipoMovimiento(null, " "), 1, null, null);
        assertThrows(ReglaNegocioException.class, m3::validarDatosObligatorios);
        MovimientoInventario mValidoPorNombre = new MovimientoInventario(null, "P1", 1, new TipoMovimiento(null, "Ingreso por Compra"), 2, null, "OK");
        mValidoPorNombre.validarDatosObligatorios();
        assertNotNull(mValidoPorNombre.getFechaMovimiento());
    }

    @Test
    void calendarioLaboralEvaluaDomingosFestivosYSiguienteDiaHabil() {
        // Arrange
        CalendarioFestivosGateway gateway = mock(CalendarioFestivosGateway.class);
        when(gateway.obtenerFestivos(2026)).thenReturn(List.of(
                new DiaFestivo(LocalDate.of(2026, 11, 16), "Independencia de Cartagena", DiaFestivo.FUENTE_CACHE),
                new DiaFestivo(LocalDate.of(2026, 12, 25), "Navidad", DiaFestivo.FUENTE_CACHE)
        ));
        when(gateway.obtenerFestivos(2025)).thenReturn(List.of());
        CalendarioLaboral calendario = new CalendarioLaboral(gateway);

        // Act
        boolean sabado14Habil = calendario.esDiaHabil(LocalDate.of(2026, 11, 14));
        boolean domingo15Habil = calendario.esDiaHabil(LocalDate.of(2026, 11, 15));
        boolean lunesFestivo16Habil = calendario.esDiaHabil(LocalDate.of(2026, 11, 16));
        boolean dia2025SinFestivos = calendario.esDiaHabil(LocalDate.of(2025, 5, 6));
        LocalDate siguienteDesdeSabado14 = calendario.siguienteDiaHabil(LocalDate.of(2026, 11, 14));
        int diasHabilesSemana = calendario.contarDiasHabiles(
                LocalDate.of(2026, 11, 14),
                LocalDate.of(2026, 11, 18)
        );

        // Assert
        assertTrue(sabado14Habil);
        assertFalse(domingo15Habil);
        assertFalse(lunesFestivo16Habil);
        assertTrue(dia2025SinFestivos);
        assertEquals(LocalDate.of(2026, 11, 17), siguienteDesdeSabado14);
        assertEquals(3, diasHabilesSemana);
        assertThrows(ReglaNegocioException.class, () -> calendario.esDiaHabil(null));
        assertThrows(ReglaNegocioException.class, () -> calendario.siguienteDiaHabil(null));
        assertThrows(ReglaNegocioException.class,
                () -> calendario.contarDiasHabiles( null, LocalDate.of(2026, 11, 10)));
        assertThrows(ReglaNegocioException.class,
                () -> calendario.contarDiasHabiles(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 10)));
    }
}
