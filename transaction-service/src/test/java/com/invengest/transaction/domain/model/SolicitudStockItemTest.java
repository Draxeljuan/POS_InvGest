package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolicitudStockItemTest {

    @Test
    @DisplayName("Debe construir y acceder a las propiedades de SolicitudStockItem")
    void debeConstruirYAccederPropiedades() {
        // Arrange
        SolicitudStockItem item = new SolicitudStockItem();

        // Act
        item.setIdProducto("PROD-01");
        item.setCantidad(4);
        SolicitudStockItem itemFull = new SolicitudStockItem("PROD-02", 2);

        // Assert
        assertEquals("PROD-01", item.getIdProducto());
        assertEquals(4, item.getCantidad());
        assertEquals("PROD-02", itemFull.getIdProducto());
        assertEquals(2, itemFull.getCantidad());
    }
}
