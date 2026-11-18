package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductoCatalogoTest {

    @Test
    @DisplayName("Debe construir y acceder a las propiedades de ProductoCatalogo")
    void debeConstruirYAccederPropiedades() {
        // Arrange
        ProductoCatalogo prod = new ProductoCatalogo();

        // Act
        prod.setIdProducto("PROD-01");
        prod.setNombre("Cuaderno");
        prod.setPrecioVenta(new BigDecimal("8500.00"));
        prod.setStock(20);
        prod.setStockMinimo(5);
        ProductoCatalogo prodFull = new ProductoCatalogo("PROD-02", "Lápiz", new BigDecimal("1500.00"), 50, 10);

        // Assert
        assertEquals("PROD-01", prod.getIdProducto());
        assertEquals("Cuaderno", prod.getNombre());
        assertEquals(new BigDecimal("8500.00"), prod.getPrecioVenta());
        assertEquals(20, prod.getStock());
        assertEquals(5, prod.getStockMinimo());
        assertEquals("PROD-02", prodFull.getIdProducto());
    }
}
