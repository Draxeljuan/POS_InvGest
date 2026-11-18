package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CierreCajaDetalleTest {

    @Test
    @DisplayName("Debe construir y acceder a las propiedades de CierreCajaDetalle")
    void debeConstruirYAccederPropiedades() {
        // Arrange
        MetodoPago mp = new MetodoPago(1, "Efectivo");
        CierreCajaDetalle detalle = new CierreCajaDetalle();

        // Act
        detalle.setIdDetalle(10);
        detalle.setIdCierre(2);
        detalle.setMetodoPago(mp);
        detalle.setTotalMetodo(new BigDecimal("45000.00"));
        detalle.setCantidadOperaciones(3);
        CierreCajaDetalle detalleFull = new CierreCajaDetalle(11, 2, mp, new BigDecimal("15000.00"), 1);

        // Assert
        assertEquals(10, detalle.getIdDetalle());
        assertEquals(2, detalle.getIdCierre());
        assertEquals(mp, detalle.getMetodoPago());
        assertEquals(new BigDecimal("45000.00"), detalle.getTotalMetodo());
        assertEquals(3, detalle.getCantidadOperaciones());
        assertEquals(11, detalleFull.getIdDetalle());
    }
}
