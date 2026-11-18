package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumenCajaTest {

    @Test
    @DisplayName("Debe construir y modificar propiedades de ResumenCaja")
    void debeConstruirYModificarPropiedades() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        ResumenMetodoPago rmp = new ResumenMetodoPago(1, "Efectivo", new BigDecimal("20000.00"), 2);
        ResumenCaja resumen = new ResumenCaja();

        // Act
        resumen.setFecha(fecha);
        resumen.setTotalVentasCompletadas(new BigDecimal("20000.00"));
        resumen.setCantidadVentasCompletadas(2);
        resumen.setTotalVentasAnuladas(new BigDecimal("5000.00"));
        resumen.setCantidadVentasAnuladas(1);
        resumen.setEfectivoEsperado(new BigDecimal("20000.00"));
        resumen.setTotalesPorMetodoPago(List.of(rmp));
        resumen.setCierreYaRegistrado(true);

        // Assert
        assertEquals(fecha, resumen.getFecha());
        assertEquals(new BigDecimal("20000.00"), resumen.getTotalVentasCompletadas());
        assertEquals(2, resumen.getCantidadVentasCompletadas());
        assertEquals(new BigDecimal("5000.00"), resumen.getTotalVentasAnuladas());
        assertEquals(1, resumen.getCantidadVentasAnuladas());
        assertEquals(new BigDecimal("20000.00"), resumen.getEfectivoEsperado());
        assertEquals(1, resumen.getTotalesPorMetodoPago().size());
        assertTrue(resumen.getCierreYaRegistrado());
    }
}
