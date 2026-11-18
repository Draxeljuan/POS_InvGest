package com.invengest.auth.domain.model;

import com.invengest.auth.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    @Test
    @DisplayName("Debe gestionar activación, desactivación, último acceso y nombre completo de Vendedor")
    void metodosComportamiento_CuandoVendedor_DebeEjecutarCorrectamente() {
        // Arrange
        RolUsuario rolVendedor = new RolUsuario(2, RolUsuario.ROL_VENDEDOR);
        Usuario vendedor = new Usuario(2, " Laura ", " Gómez ", "l@m.com", "300", null, rolVendedor, "lgomez", "hash", true);
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 17, 22, 0);

        // Act
        vendedor.desactivar();
        boolean activoTrasDesactivar = vendedor.estaActivo();
        vendedor.activar();
        vendedor.registrarUltimoAcceso(ahora);
        String nombreCompleto = vendedor.obtenerNombreCompleto();

        // Assert
        assertFalse(activoTrasDesactivar);
        assertTrue(vendedor.estaActivo());
        assertFalse(vendedor.esAdministrador());
        assertEquals(ahora, vendedor.getUltimoAcceso());
        assertEquals("Laura Gómez", nombreCompleto);
    }

    @Test
    @DisplayName("Debe impedir desactivar a un usuario con rol Administrador")
    void desactivar_CuandoEsAdministrador_DebeLanzarReglaNegocioException() {
        // Arrange
        RolUsuario rolAdmin = new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR);
        Usuario admin = new Usuario(1, null, null, "a@m.com", "300", null, rolAdmin, "admin", "hash", true);

        // Act & Assert
        assertTrue(admin.esAdministrador());
        assertEquals("", admin.obtenerNombreCompleto());
        assertThrows(ReglaNegocioException.class, admin::desactivar);
    }
}
