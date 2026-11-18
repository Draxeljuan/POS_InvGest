package com.invengest.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionItemStockTest {

    @Test
    @DisplayName("Construye y expone atributos de ValidacionItemStock")
    void construyeYExponeAtributos() {
        // Arrange & Act
        ValidacionItemStock validacion = new ValidacionItemStock(
                "PROD-0001",
                "Marcador",
                new BigDecimal("3500.00"),
                10,
                2,
                true
        );

        // Assert
        assertEquals("PROD-0001", validacion.idProducto());
        assertEquals("Marcador", validacion.nombre());
        assertEquals(new BigDecimal("3500.00"), validacion.precioVenta());
        assertEquals(10, validacion.stockDisponible());
        assertEquals(2, validacion.cantidadSolicitada());
        assertTrue(validacion.suficiente());
    }
}
