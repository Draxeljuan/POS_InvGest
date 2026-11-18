package com.invengest.analytics.infrastructure.entry_points.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class JsonAuthenticationEntryPointTest {

    @Test
    @DisplayName("Debe escribir respuesta HTTP 401 en JSON escapando comillas y aceptando mensaje nulo")
    void commenceYWriteUnauthorized_EscribeJson401() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response401 = new MockHttpServletResponse();
        MockHttpServletResponse responseNull = new MockHttpServletResponse();

        // Act
        entryPoint.commence(request, response401, new BadCredentialsException("No auth"));
        entryPoint.writeUnauthorized(responseNull, null);

        // Assert
        assertThat(response401.getStatus()).isEqualTo(401);
        assertThat(response401.getContentAsString()).contains("\"status\":401");
        assertThat(responseNull.getStatus()).isEqualTo(401);
    }
}
