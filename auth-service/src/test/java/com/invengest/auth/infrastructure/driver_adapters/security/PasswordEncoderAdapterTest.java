package com.invengest.auth.infrastructure.driver_adapters.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncoderAdapterTest {

    @Test
    @DisplayName("Debe codificar y verificar contraseñas con BCrypt")
    void encodeAndMatches_DebeCodificarYValidar() {
        // Arrange
        PasswordEncoderAdapter encoder = new PasswordEncoderAdapter();

        // Act
        String encoded = encoder.encode("SecretPass123!");

        // Assert
        assertNotEquals("SecretPass123!", encoded);
        assertTrue(encoder.matches("SecretPass123!", encoded));
    }
}
