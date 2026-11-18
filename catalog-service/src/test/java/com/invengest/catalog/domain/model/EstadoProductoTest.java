package com.invengest.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoProductoTest {

    @Test
    @DisplayName("Identifica correctamente estados Activo, Agotado y Descontinuado")
    void identificaEstadosCorrectamente() {
        // Arrange
        EstadoProducto activo = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        EstadoProducto descontinuado = new EstadoProducto(2, EstadoProducto.ESTADO_DESCONTINUADO);
        EstadoProducto agotado = new EstadoProducto(3, EstadoProducto.ESTADO_AGOTADO);
        EstadoProducto nulo = new EstadoProducto();
        nulo.setIdEstado(4);
        nulo.setNombre(null);

        // Act
        boolean esActivo = activo.esActivo();
        boolean esDescontinuado = descontinuado.esDescontinuado();
        boolean esAgotado = agotado.esAgotado();

        // Assert
        assertEquals(1, activo.getIdEstado());
        assertTrue(esActivo);
        assertFalse(activo.esAgotado());
        assertTrue(esDescontinuado);
        assertTrue(esAgotado);
        assertFalse(nulo.esActivo());
        assertFalse(nulo.esAgotado());
        assertFalse(nulo.esDescontinuado());
    }
}
