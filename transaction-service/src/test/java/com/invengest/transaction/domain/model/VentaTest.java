package com.invengest.transaction.domain.model;

import com.invengest.transaction.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VentaTest {

    @Test
    @DisplayName("Debe validar cabecera, calcular total, marcar completada y anular venta correctamente")
    void debeCalcularTotalMarcarCompletadaYAnularVenta() {
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
        List<DetalleVenta> productos = venta.obtenerDetallesProducto();
        venta.anular(99, " Error en digitación ", null);

        // Assert
        assertEquals(new BigDecimal("19000.00"), total);
        assertNotNull(venta.getFecha());
        assertEquals(1, productos.size());
        assertTrue(venta.estaAnulada());
        assertFalse(venta.estaCompletada());
        assertEquals(99, venta.getIdUsuarioAnulacion());
        assertEquals("Error en digitación", venta.getMotivoAnulacion());
        assertNotNull(venta.getFechaAnulacion());
    }

    @Test
    @DisplayName("Debe validar ramas de cabecera incompleta, lista nula y estados de anulación")
    void debeValidarErroresDeCabeceraYAnulacion() {
        // Arrange
        LocalDateTime fechaPrevia = LocalDateTime.of(2026, 11, 16, 10, 0);
        Venta ventaConFecha = new Venta(1, 1, fechaPrevia, null, new MetodoPago(1, "Efectivo"),
                BigDecimal.ZERO, null, null, null, null, null);
        Venta ventaSinUsuario = new Venta();
        Venta ventaSinMetodo = new Venta();
        ventaSinMetodo.setIdUsuario(1);
        Venta ventaMetodoIdNulo = new Venta();
        ventaMetodoIdNulo.setIdUsuario(1);
        ventaMetodoIdNulo.setMetodoPago(new MetodoPago(null, "Efectivo"));
        Venta ventaSinDetalles = new Venta();
        ventaSinDetalles.setIdUsuario(1);
        ventaSinDetalles.setMetodoPago(new MetodoPago(1, "Efectivo"));
        ventaSinDetalles.setDetalles(List.of());

        // Act
        boolean completadaEstadoNulo = ventaConFecha.estaCompletada();
        BigDecimal totalSinDetalles = ventaConFecha.calcularTotal();
        ventaConFecha.marcarCompletada(LocalDateTime.now());
        List<DetalleVenta> detallesProdNulos = ventaConFecha.obtenerDetallesProducto();

        // Assert
        assertTrue(completadaEstadoNulo);
        assertEquals(new BigDecimal("0.00"), totalSinDetalles);
        assertEquals(fechaPrevia, ventaConFecha.getFecha());
        assertTrue(detallesProdNulos.isEmpty());
        assertThrows(ReglaNegocioException.class, ventaSinUsuario::validarCabecera);
        assertThrows(ReglaNegocioException.class, ventaSinMetodo::validarCabecera);
        assertThrows(ReglaNegocioException.class, ventaMetodoIdNulo::validarCabecera);
        assertThrows(ReglaNegocioException.class, ventaSinDetalles::validarCabecera);

        assertThrows(ReglaNegocioException.class, () -> ventaConFecha.anular(null, "Motivo", fechaPrevia));
        assertThrows(ReglaNegocioException.class, () -> ventaConFecha.anular(1, null, fechaPrevia));
        assertThrows(ReglaNegocioException.class, () -> ventaConFecha.anular(1, "   ", fechaPrevia));
        ventaConFecha.anular(1, "Anulada", fechaPrevia);
        assertThrows(ReglaNegocioException.class, () -> ventaConFecha.anular(1, "Doble anulación", fechaPrevia));
    }
}
