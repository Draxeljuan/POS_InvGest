package com.invengest.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoProveedorTest {

    @Test
    @DisplayName("Identifica correctamente estados Activo e Inactivo de proveedor")
    void identificaEstadosProveedorCorrectamente() {
        // Arrange
        EstadoProveedor activo = new EstadoProveedor(1, EstadoProveedor.ESTADO_ACTIVO);
        EstadoProveedor inactivo = new EstadoProveedor(2, EstadoProveedor.ESTADO_INACTIVO);
        EstadoProveedor nulo = new EstadoProveedor();
        nulo.setIdEstadoProveedor(3);
        nulo.setNombre(null);

        // Act
        boolean esActivo = activo.esActivo();
        boolean esInactivo = inactivo.esInactivo();

        // Assert
        assertEquals(1, activo.getIdEstadoProveedor());
        assertTrue(esActivo);
        assertFalse(activo.esInactivo());
        assertTrue(esInactivo);
        assertFalse(inactivo.esActivo());
        assertFalse(nulo.esActivo());
        assertFalse(nulo.esInactivo());
    }
}
