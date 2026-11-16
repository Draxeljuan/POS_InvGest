package com.invengest.gateway;

import com.invengest.gateway.domain.exception.TokenInvalidoException;
import com.invengest.gateway.domain.model.DecisionAccesoRuta;
import com.invengest.gateway.domain.model.PolizaSeguridadGateway;
import com.invengest.gateway.domain.model.TokenClaims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainModelsAndPolizaTest {

    @Test
    void tokenClaims_WhenValid_ShouldPassValidationAndIdentifyRole() {
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
    void tokenClaims_WhenMissingFields_ShouldThrowTokenInvalidoException() {
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

    @Test
    void decisionAccesoRuta_FactoryMethods_ShouldCreateExpectedInstances() {
        // Arrange
        TokenClaims claims = new TokenClaims("admin", "Administrador", "1", "invengest-pos-auth");

        // Act
        DecisionAccesoRuta publica = DecisionAccesoRuta.permitirRutaPublica();
        DecisionAccesoRuta protegida = DecisionAccesoRuta.autorizarRutaProtegida(claims);

        // Assert
        assertTrue(publica.rutaPublica());
        assertNull(publica.tokenClaims());
        assertFalse(protegida.rutaPublica());
        assertEquals(claims, protegida.tokenClaims());
    }

    @Test
    void polizaSeguridad_NormalizarRutaYDetectarRutasInternas() {
        // Arrange & Act & Assert
        assertEquals("/", PolizaSeguridadGateway.normalizarRuta(null));
        assertEquals("/", PolizaSeguridadGateway.normalizarRuta("   "));
        assertEquals("/api/auth/login", PolizaSeguridadGateway.normalizarRuta("api/auth/login/"));
        assertEquals("/api/catalog/productos", PolizaSeguridadGateway.normalizarRuta("//api///catalog//productos/"));
        assertEquals("/ruta con espacios", PolizaSeguridadGateway.normalizarRuta("ruta con espacios"));

        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada("/internal"));
        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada("/internal/productos/validar-stock"));
        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada("/api/analytics/internal/ventas"));
        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada("/api/analytics/internal"));
        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada("/api/auth/../internal/usuarios"));
        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada("/api/auth/%2e%2e/internal/usuarios"));
        assertFalse(PolizaSeguridadGateway.esRutaInternaBloqueada("/api/catalog/productos"));
    }

    @Test
    void polizaSeguridad_RutasPublicasYHeadersSensibles() {
        // Arrange & Act & Assert
        assertTrue(PolizaSeguridadGateway.esRutaPublica("OPTIONS", "/api/transactions/ventas"));
        assertTrue(PolizaSeguridadGateway.esRutaPublica("POST", "/api/auth/login"));
        assertTrue(PolizaSeguridadGateway.esRutaPublica("POST", "/api/auth/register"));
        assertTrue(PolizaSeguridadGateway.esRutaPublica("GET", "/actuator/health"));
        assertTrue(PolizaSeguridadGateway.esRutaPublica("GET", "/actuator/health/readiness"));

        assertFalse(PolizaSeguridadGateway.esRutaPublica("GET", "/api/auth/login"));
        assertFalse(PolizaSeguridadGateway.esRutaPublica("GET", "/api/auth/me"));
        assertFalse(PolizaSeguridadGateway.esRutaPublica(null, "/api/catalog/productos"));

        assertTrue(PolizaSeguridadGateway.esHeaderSensibleSanitizable("X-User-Id"));
        assertTrue(PolizaSeguridadGateway.esHeaderSensibleSanitizable("x-user-role"));
        assertTrue(PolizaSeguridadGateway.esHeaderSensibleSanitizable("X-User-Name"));
        assertTrue(PolizaSeguridadGateway.esHeaderSensibleSanitizable("X-Internal-Api-Key"));
        assertTrue(PolizaSeguridadGateway.esHeaderSensibleSanitizable("x-internal-api-key"));
        assertFalse(PolizaSeguridadGateway.esHeaderSensibleSanitizable("Authorization"));
        assertFalse(PolizaSeguridadGateway.esHeaderSensibleSanitizable("Content-Type"));
        assertFalse(PolizaSeguridadGateway.esHeaderSensibleSanitizable(null));
        assertFalse(PolizaSeguridadGateway.esHeaderSensibleSanitizable("   "));
    }
}
