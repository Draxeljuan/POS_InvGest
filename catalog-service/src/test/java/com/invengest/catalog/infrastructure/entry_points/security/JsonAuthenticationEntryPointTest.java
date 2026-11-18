package com.invengest.catalog.infrastructure.entry_points.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAuthenticationEntryPointTest {

    @Test
    @DisplayName("Retorna JSON estructurado con código 401 ante falta de autenticación")
    void retornaJsonPara401() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        MockHttpServletResponse res401 = new MockHttpServletResponse();

        // Act
        entryPoint.commence(new MockHttpServletRequest(), res401, new BadCredentialsException("Sin token"));

        // Assert
        assertEquals(401, res401.getStatus());
        assertTrue(res401.getContentAsString().contains("\"status\":401"));
    }
}
