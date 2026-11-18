package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteTest {

    @Test
    @DisplayName("Debe construir nombre completo y validar formato de correo electrónico")
    void debeConstruirNombreCompletoYValidarEmail() {
        // Arrange
        Cliente completo = new Cliente(1, new TipoDocumento(1, "CC"), "1001",
                "Juan", "Carlos", "Pérez", "Gómez", "300", "juan@test.com");
        Cliente parcial = new Cliente(2, null, "1002", "Ana", " ", "Gil", "", "310", "invalido");
        Cliente soloApellido = new Cliente(3, null, null, null, null, "Ruiz", null, null, "  ");
        Cliente sinEmail = new Cliente();

        // Act
        String nombreCompleto = completo.getNombreCompleto();
        String nombreParcial = parcial.getNombreCompleto();
        String nombreSoloApellido = soloApellido.getNombreCompleto();

        // Assert
        assertEquals("Juan Carlos Pérez Gómez", nombreCompleto);
        assertEquals("Ana Gil", nombreParcial);
        assertEquals("Ruiz", nombreSoloApellido);
        assertTrue(completo.tieneEmailValido());
        assertFalse(parcial.tieneEmailValido());
        assertFalse(soloApellido.tieneEmailValido());
        assertFalse(sinEmail.tieneEmailValido());
    }
}
