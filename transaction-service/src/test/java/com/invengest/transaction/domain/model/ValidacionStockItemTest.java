package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionStockItemTest {

    @Test
    @DisplayName("Debe construir y acceder a las propiedades de ValidacionStockItem")
    void debeConstruirYAccederPropiedades() {
        // Arrange
        ValidacionStockItem validacion = new ValidacionStockItem();

        // Act
        validacion.setIdProducto("PROD-01");
        validacion.setNombre("Resma");
        validacion.setPrecioVenta(new BigDecimal("18000.00"));
        validacion.setStockDisponible(10);
        validacion.setCantidadSolicitada(2);
        validacion.setSuficiente(true);

        // Assert
        assertEquals("PROD-01", validacion.getIdProducto());
        assertEquals("Resma", validacion.getNombre());
        assertEquals(new BigDecimal("18000.00"), validacion.getPrecioVenta());
        assertEquals(10, validacion.getStockDisponible());
        assertEquals(2, validacion.getCantidadSolicitada());
        assertTrue(validacion.isSuficiente());
    }
}
