package com.invengest.transaction.infrastructure.entry_points.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAuthenticationEntryPointTest {

    @Test
    @DisplayName("Debe responder 401 Unauthorized con cuerpo JSON estándar")
    void debeResponder401ConJsonEstandar() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/transactions/ventas");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        entryPoint.commence(request, response, new BadCredentialsException("Token expirado"));
        MockHttpServletResponse responseSinMsg = new MockHttpServletResponse();
        entryPoint.commence(request, responseSinMsg, new BadCredentialsException(""));

        // Assert
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("No Autorizado"));
        assertEquals(401, responseSinMsg.getStatus());
    }
}
