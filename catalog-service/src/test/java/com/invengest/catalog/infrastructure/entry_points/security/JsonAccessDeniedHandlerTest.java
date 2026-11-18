package com.invengest.catalog.infrastructure.entry_points.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAccessDeniedHandlerTest {

    @Test
    @DisplayName("Retorna JSON estructurado con código 403 ante acceso denegado")
    void retornaJsonPara403() throws Exception {
        // Arrange
        JsonAccessDeniedHandler accessDeniedHandler = new JsonAccessDeniedHandler();
        MockHttpServletResponse res403 = new MockHttpServletResponse();

        // Act
        accessDeniedHandler.handle(new MockHttpServletRequest(), res403, new AccessDeniedException("Prohibido"));

        // Assert
        assertEquals(403, res403.getStatus());
        assertTrue(res403.getContentAsString().contains("\"status\":403"));
    }
}
