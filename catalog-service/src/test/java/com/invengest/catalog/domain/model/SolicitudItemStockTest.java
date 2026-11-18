package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolicitudItemStockTest {

    @Test
    @DisplayName("Normaliza idProducto y valida cantidad mayor a cero")
    void normalizaIdProductoYValidaCantidad() {
        // Arrange & Act
        SolicitudItemStock item = new SolicitudItemStock(" PROD-0001 ", 3);

        // Assert
        assertEquals("PROD-0001", item.idProducto());
        assertEquals(3, item.cantidad());
    }

    @Test
    @DisplayName("Rechaza código de producto vacío o cantidad menor o igual a cero")
    void rechazaDatosInvalidos() {
        // Arrange, Act & Assert
        assertThrows(ReglaNegocioException.class, () -> new SolicitudItemStock(null, 1));
        assertThrows(ReglaNegocioException.class, () -> new SolicitudItemStock(" ", 1));
        assertThrows(ReglaNegocioException.class, () -> new SolicitudItemStock("PROD-0001", null));
        assertThrows(ReglaNegocioException.class, () -> new SolicitudItemStock("PROD-0001", 0));
    }
}
