package com.invengest.auth.infrastructure.entry_points.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAuthenticationEntryPointTest {

    @Test
    @DisplayName("Debe escribir respuesta JSON 401 estándar")
    void commence_DebeEscribirJson401() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res401 = new MockHttpServletResponse();

        // Act
        entryPoint.commence(req, res401, new BadCredentialsException("No token"));

        // Assert
        assertEquals(401, res401.getStatus());
        assertTrue(res401.getContentAsString().contains("\"status\":401"));
    }
}
