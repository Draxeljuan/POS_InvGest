package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResumenMetodoPagoTest {

    @Test
    @DisplayName("Debe construir y actualizar propiedades de ResumenMetodoPago")
    void debeConstruirYActualizarPropiedades() {
        // Arrange
        ResumenMetodoPago resumen = new ResumenMetodoPago();

        // Act
        resumen.setIdMetodoPago(1);
        resumen.setNombreMetodoPago("Efectivo");
        resumen.setTotal(new BigDecimal("35000.00"));
        resumen.setCantidadOperaciones(4);

        // Assert
        assertEquals(1, resumen.getIdMetodoPago());
        assertEquals("Efectivo", resumen.getNombreMetodoPago());
        assertEquals(new BigDecimal("35000.00"), resumen.getTotal());
        assertEquals(4, resumen.getCantidadOperaciones());
    }
}
