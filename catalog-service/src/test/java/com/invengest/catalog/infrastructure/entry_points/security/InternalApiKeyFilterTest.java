package com.invengest.catalog.infrastructure.entry_points.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class InternalApiKeyFilterTest {

    private static final String INTERNAL_KEY = "internal-key-2026";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Autoriza llave interna válida en /internal/** y rechaza con 401 llave inválida")
    void autorizaLlaveValidaYRechaza401EnLlaveInvalida() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        InternalApiKeyFilter filter = new InternalApiKeyFilter(INTERNAL_KEY, entryPoint);
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest reqValida = new MockHttpServletRequest("POST", "/internal/productos/validar-stock");
        reqValida.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, INTERNAL_KEY);
        MockHttpServletResponse resValida = new MockHttpServletResponse();

        MockHttpServletRequest reqInvalida = new MockHttpServletRequest("POST", "/internal/productos/descontar-stock");
        reqInvalida.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, "llave-falsa");
        MockHttpServletResponse resInvalida = new MockHttpServletResponse();

        MockHttpServletRequest reqPublica = new MockHttpServletRequest("GET", "/api/catalog/productos");
        MockHttpServletResponse resPublica = new MockHttpServletResponse();

        // Act
        filter.doFilter(reqValida, resValida, chain);
        filter.doFilter(reqInvalida, resInvalida, chain);
        filter.doFilter(reqPublica, resPublica, chain);

        // Assert
        verify(chain).doFilter(reqValida, resValida);
        verify(chain, never()).doFilter(reqInvalida, resInvalida);
        verify(chain).doFilter(reqPublica, resPublica);
        assertEquals(401, resInvalida.getStatus());
        assertTrue(resInvalida.getContentAsString().contains("X-Internal-Api-Key"));
    }
}
