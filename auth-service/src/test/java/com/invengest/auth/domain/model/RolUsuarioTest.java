package com.invengest.auth.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolUsuarioTest {

    @Test
    @DisplayName("Debe identificar si el rol corresponde a Administrador")
    void esAdministrador_CuandoNombreCoincideONo_DebeRetornarBooleanoEsperado() {
        // Arrange
        RolUsuario admin = new RolUsuario(1, "Administrador");
        RolUsuario vendedor = new RolUsuario();
        vendedor.setIdRol(2);
        vendedor.setNombre("Vendedor");

        // Act & Assert
        assertTrue(admin.esAdministrador());
        assertFalse(vendedor.esAdministrador());
        assertEquals(2, vendedor.getIdRol());
        assertEquals("Vendedor", vendedor.getNombre());
    }
}
