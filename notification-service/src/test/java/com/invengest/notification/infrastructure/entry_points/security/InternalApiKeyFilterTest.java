package com.invengest.notification.infrastructure.entry_points.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class InternalApiKeyFilterTest {

    private static final String INTERNAL_KEY = "internal_test_key_2026";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe autorizar petición a /internal/** con llave válida")
    void doFilterInternal_CuandoLlaveValida_DebeEstablecerRoleService() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        InternalApiKeyFilter filter = new InternalApiKeyFilter(INTERNAL_KEY, entryPoint);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/internal/notificaciones/bienvenida");
        request.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, INTERNAL_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(InternalApiKeyFilter.AUTHORITY_INTERNAL_SERVICE)));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Debe rechazar con 401 JSON cuando falta X-Internal-Api-Key o es inválida en /internal/**")
    void doFilterInternal_CuandoLlaveInvalida_DebeRetornar401Json() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        InternalApiKeyFilter filter = new InternalApiKeyFilter(INTERNAL_KEY, entryPoint);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/internal/notificaciones/alerta-stock");
        request.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, "llave-incorrecta");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("X-Internal-Api-Key"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Debe ignorar rutas públicas o de /api/**")
    void doFilterInternal_CuandoRutaNoInterna_DebeContinuarCadena() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        InternalApiKeyFilter filter = new InternalApiKeyFilter(INTERNAL_KEY, entryPoint);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/notificaciones/prueba");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }
}
