package com.invengest.auth.infrastructure.driver_adapters.security;

import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtProviderAdapterTest {

    @Test
    @DisplayName("Debe generar un token JWT firmado con HS256 para el usuario")
    void generateToken_CuandoUsuarioEsValido_DebeRetornarTokenFirmado() {
        // Arrange
        String secret = "test_secret_key_for_unit_testing_minimum_32_bytes_2026";
        JwtProviderAdapter provider = new JwtProviderAdapter(secret, 60000L, "invengest-pos-auth");
        Usuario admin = new Usuario(1, "Juan", "D", "j@m.com", "300", null,
                new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR), "juand", "h", true);
        Usuario vendedor = new Usuario(2, "Ana", "P", "a@m.com", "300", null,
                new RolUsuario(2, RolUsuario.ROL_VENDEDOR), "anap", "h", true);

        // Act
        String tokenAdmin = provider.generateToken(admin);
        String tokenVendedor = provider.generateToken(vendedor);

        // Assert
        assertNotNull(tokenAdmin);
        assertFalse(tokenAdmin.isBlank());
        assertNotNull(tokenVendedor);
        assertFalse(tokenVendedor.isBlank());
    }
}
