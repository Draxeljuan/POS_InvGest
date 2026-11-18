package com.invengest.gateway.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolizaSeguridadGatewayTest {

    @Test
    @DisplayName("Debe normalizar rutas y detectar rutas internas bloqueadas incluyendo path traversal")
    void normalizarRutaYDetectarRutasInternas_DebeFuncionarCorrectamente() {
        // Arrange
        String rutaConTraversal = "/api/auth/../internal/usuarios";
        String rutaConEncoding = "/api/auth/%2e%2e/internal/usuarios";

        // Act & Assert
        assertEquals("/", PolizaSeguridadGateway.normalizarRuta(null));
        assertEquals("/", PolizaSeguridadGateway.normalizarRuta("   "));
        assertEquals("/api/auth/login", PolizaSeguridadGateway.normalizarRuta("api/auth/login/"));
        assertEquals("/api/catalog/productos", PolizaSeguridadGateway.normalizarRuta("//api///catalog//productos/"));
        assertEquals("/ruta con espacios", PolizaSeguridadGateway.normalizarRuta("ruta con espacios"));

        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada("/internal"));
        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada("/internal/productos/validar-stock"));
        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada("/api/analytics/internal/ventas"));
        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada("/api/analytics/internal"));
        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada(rutaConTraversal));
        assertTrue(PolizaSeguridadGateway.esRutaInternaBloqueada(rutaConEncoding));
        assertFalse(PolizaSeguridadGateway.esRutaInternaBloqueada("/api/catalog/productos"));
    }

    @Test
    @DisplayName("Debe identificar rutas públicas y cabeceras sensibles sanitizables")
    void rutasPublicasYHeadersSensibles_DebeEvaluarCorrectamente() {
        // Arrange
        String headerUserId = "X-User-Id";
        String headerInternalKey = "X-Internal-Api-Key";

        // Act & Assert
        assertTrue(PolizaSeguridadGateway.esRutaPublica("OPTIONS", "/api/transactions/ventas"));
        assertTrue(PolizaSeguridadGateway.esRutaPublica("POST", "/api/auth/login"));
        assertTrue(PolizaSeguridadGateway.esRutaPublica("POST", "/api/auth/register"));
        assertTrue(PolizaSeguridadGateway.esRutaPublica("GET", "/actuator/health"));
        assertTrue(PolizaSeguridadGateway.esRutaPublica("GET", "/actuator/health/readiness"));

        assertFalse(PolizaSeguridadGateway.esRutaPublica("GET", "/api/auth/login"));
        assertFalse(PolizaSeguridadGateway.esRutaPublica("GET", "/api/auth/me"));
        assertFalse(PolizaSeguridadGateway.esRutaPublica(null, "/api/catalog/productos"));

        assertTrue(PolizaSeguridadGateway.esHeaderSensibleSanitizable(headerUserId));
        assertTrue(PolizaSeguridadGateway.esHeaderSensibleSanitizable("x-user-role"));
        assertTrue(PolizaSeguridadGateway.esHeaderSensibleSanitizable("X-User-Name"));
        assertTrue(PolizaSeguridadGateway.esHeaderSensibleSanitizable(headerInternalKey));
        assertTrue(PolizaSeguridadGateway.esHeaderSensibleSanitizable("x-internal-api-key"));
        assertFalse(PolizaSeguridadGateway.esHeaderSensibleSanitizable("Authorization"));
        assertFalse(PolizaSeguridadGateway.esHeaderSensibleSanitizable("Content-Type"));
        assertFalse(PolizaSeguridadGateway.esHeaderSensibleSanitizable(null));
        assertFalse(PolizaSeguridadGateway.esHeaderSensibleSanitizable("   "));
    }
}
