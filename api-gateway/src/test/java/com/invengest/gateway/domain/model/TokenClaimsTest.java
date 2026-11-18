package com.invengest.gateway.domain.model;

import com.invengest.gateway.domain.exception.TokenInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenClaimsTest {

    @Test
    @DisplayName("Debe validar identidad completa e identificar roles Administrador y Vendedor")
    void validarIdentidadCompleta_CuandoClaimsSonValidos_DebeIdentificarRol() {
        // Arrange
        TokenClaims adminClaims = new TokenClaims("admin", "Administrador", "1", "invengest-pos-auth");
        TokenClaims vendedorClaims = new TokenClaims("vendedor1", "Vendedor", "2", "invengest-pos-auth");

        // Act & Assert
        assertDoesNotThrow(adminClaims::validarIdentidadCompleta);
        assertTrue(adminClaims.esAdministrador());
        assertFalse(adminClaims.esVendedor());

        assertDoesNotThrow(vendedorClaims::validarIdentidadCompleta);
        assertFalse(vendedorClaims.esAdministrador());
        assertTrue(vendedorClaims.esVendedor());
    }

    @Test
    @DisplayName("Debe lanzar TokenInvalidoException cuando faltan claims obligatorios")
    void validarIdentidadCompleta_CuandoFaltanCampos_DebeLanzarExcepcion() {
        // Arrange
        TokenClaims sinSubject = new TokenClaims(" ", "Administrador", "1", "invengest-pos-auth");
        TokenClaims sinRole = new TokenClaims("admin", "", "1", "invengest-pos-auth");
        TokenClaims sinUserId = new TokenClaims("admin", "Administrador", null, "invengest-pos-auth");
        TokenClaims userIdNullLiteral = new TokenClaims("admin", "Administrador", "null", "invengest-pos-auth");

        // Act & Assert
        assertThrows(TokenInvalidoException.class, sinSubject::validarIdentidadCompleta);
        assertThrows(TokenInvalidoException.class, sinRole::validarIdentidadCompleta);
        assertThrows(TokenInvalidoException.class, sinUserId::validarIdentidadCompleta);
        assertThrows(TokenInvalidoException.class, userIdNullLiteral::validarIdentidadCompleta);
        assertFalse(sinRole.esAdministrador());
        assertFalse(sinRole.esVendedor());
    }
}
