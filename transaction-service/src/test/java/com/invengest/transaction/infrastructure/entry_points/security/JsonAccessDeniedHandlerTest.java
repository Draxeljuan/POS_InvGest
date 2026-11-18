package com.invengest.transaction.infrastructure.entry_points.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAccessDeniedHandlerTest {

    @Test
    @DisplayName("Debe responder 403 Forbidden con cuerpo JSON estándar")
    void debeResponder403ConJsonEstandar() throws Exception {
        // Arrange
        JsonAccessDeniedHandler handler = new JsonAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/transactions/ventas/1/anular");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        handler.handle(request, response, new AccessDeniedException("Acceso denegado"));

        // Assert
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("Acceso Denegado"));
    }
}
