package com.invengest.auth.infrastructure.entry_points.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAccessDeniedHandlerTest {

    @Test
    @DisplayName("Debe escribir respuesta JSON 403 estándar")
    void handle_DebeEscribirJson403() throws Exception {
        // Arrange
        JsonAccessDeniedHandler accessDeniedHandler = new JsonAccessDeniedHandler();
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res403 = new MockHttpServletResponse();

        // Act
        accessDeniedHandler.handle(req, res403, new AccessDeniedException("Forbidden"));

        // Assert
        assertEquals(403, res403.getStatus());
        assertTrue(res403.getContentAsString().contains("\"status\":403"));
    }
}
