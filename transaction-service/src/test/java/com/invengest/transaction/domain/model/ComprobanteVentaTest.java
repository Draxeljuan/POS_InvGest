package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComprobanteVentaTest {

    @Test
    @DisplayName("Debe generar comprobante desde venta con cliente completo y desde venta anónima")
    void debeGenerarComprobanteDesdeVentaCompletaYAnonima() {
        // Arrange
        Cliente cliente = new Cliente(1, new TipoDocumento(1, "CC"), "1001", "Juan", "Carlos", "Pérez", "Gómez", "300", "juan@test.com");
        DetalleVenta d1 = new DetalleVenta(1, 15, "PROD-01", TipoItem.PRODUCTO, "Cuaderno",
                new BigDecimal("8500.00"), 2, new BigDecimal("17000.00"));
        Venta ventaCompleta = new Venta(15, 1, LocalDateTime.of(2026, 11, 16, 10, 0),
                cliente, new MetodoPago(1, "Efectivo"), new BigDecimal("17000.00"),
                Venta.ESTADO_COMPLETADA, null, null, null, List.of(d1));

        Venta ventaAnonima = new Venta();
        ventaAnonima.setDetalles(null);
        ventaAnonima.setCliente(new Cliente(null, null, null, "", null, "", null, null, null));

        // Act
        ComprobanteVenta compCompleto = ComprobanteVenta.desdeVenta(ventaCompleta);
        ComprobanteVenta compAnonimo = ComprobanteVenta.desdeVenta(ventaAnonima);
        ComprobanteVenta compSetters = new ComprobanteVenta();
        compSetters.setIdVenta(20);
        compSetters.setNumeroComprobante("POS-000020");

        // Assert
        assertEquals("POS-000015", compCompleto.getNumeroComprobante());
        assertEquals("Juan Carlos Pérez Gómez", compCompleto.getNombreCliente());
        assertEquals("1001", compCompleto.getDocumentoCliente());
        assertEquals("juan@test.com", compCompleto.getEmailCliente());
        assertEquals("Efectivo", compCompleto.getMetodoPago());
        assertEquals(1, compCompleto.getDetalles().size());

        assertEquals("POS-000000", compAnonimo.getNumeroComprobante());
        assertEquals("Consumidor Final", compAnonimo.getNombreCliente());
        assertNull(compAnonimo.getDocumentoCliente());
        assertEquals("Sin especificar", compAnonimo.getMetodoPago());
        assertTrue(compAnonimo.getDetalles().isEmpty());
        assertEquals(20, compSetters.getIdVenta());
    }
}
