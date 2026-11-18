package com.invengest.analytics.infrastructure.entry_points.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class JsonAccessDeniedHandlerTest {

    @Test
    @DisplayName("Debe escribir respuesta HTTP 403 en JSON estándar ante falta de permisos")
    void handle_EscribeJson403() throws Exception {
        // Arrange
        JsonAccessDeniedHandler accessDeniedHandler = new JsonAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response403 = new MockHttpServletResponse();

        // Act
        accessDeniedHandler.handle(request, response403, new AccessDeniedException("Forbidden"));

        // Assert
        assertThat(response403.getStatus()).isEqualTo(403);
        assertThat(response403.getContentAsString()).contains("\"status\":403");
        assertThat(response403.getContentAsString()).contains("Acceso Denegado");
    }
}
