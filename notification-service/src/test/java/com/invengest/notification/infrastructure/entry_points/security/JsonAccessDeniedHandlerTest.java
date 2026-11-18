package com.invengest.notification.infrastructure.entry_points.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAccessDeniedHandlerTest {

    @Test
    @DisplayName("Debe escribir respuesta JSON 403 cuando se deniega el acceso")
    void handle_DebeEscribirJson403() throws Exception {
        // Arrange
        JsonAccessDeniedHandler accessDeniedHandler = new JsonAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        accessDeniedHandler.handle(request, response, new AccessDeniedException("Denegado"));

        // Assert
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"status\":403"));
    }
}
