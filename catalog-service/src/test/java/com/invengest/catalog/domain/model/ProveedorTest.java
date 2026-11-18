package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProveedorTest {

    @Test
    @DisplayName("Valida invariantes, verifica estado activo y desactiva proveedor")
    void validarInvariantesYDesactivarProveedor() {
        // Arrange
        EstadoProveedor activo = new EstadoProveedor(1, EstadoProveedor.ESTADO_ACTIVO);
        EstadoProveedor inactivo = new EstadoProveedor(2, EstadoProveedor.ESTADO_INACTIVO);
        Proveedor proveedor = new Proveedor(
                1, activo, "  Distribuidora Norma  ", "300123", "ventas@norma.com", "Calle 10", " 900123-1 "
        );

        // Act
        proveedor.validarInvariantes();
        boolean estabaActivo = proveedor.estaActivo();
        proveedor.desactivar(inactivo);

        // Assert
        assertEquals("Distribuidora Norma", proveedor.getNombre());
        assertEquals("900123-1", proveedor.getNit());
        assertTrue(estabaActivo);
        assertFalse(proveedor.estaActivo());
        assertTrue(proveedor.getEstado().esInactivo());
    }

    @Test
    @DisplayName("Rechaza nombre vacío o estado nulo al desactivar")
    void rechazaNombreVacioOEstadoNuloAlDesactivar() {
        // Arrange
        Proveedor invalido = new Proveedor(1, null, " ", null, null, null, " ");
        Proveedor sinNit = new Proveedor();
        sinNit.setNombre("Editorial");
        sinNit.setNit("   ");

        // Act
        sinNit.validarInvariantes();

        // Assert
        assertFalse(sinNit.estaActivo());
        assertThrows(ReglaNegocioException.class, invalido::validarInvariantes);
        assertThrows(ReglaNegocioException.class, () -> sinNit.desactivar(null));
    }
}
