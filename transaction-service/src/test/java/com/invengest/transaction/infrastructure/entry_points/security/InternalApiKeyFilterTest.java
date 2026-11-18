package com.invengest.transaction.infrastructure.entry_points.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class InternalApiKeyFilterTest {

    private static final String API_KEY = "internal-key-2026";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe validar X-Internal-Api-Key en rutas /internal/ e ignorar rutas públicas")
    void debeValidarApiKeyEnRutasInternas() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        InternalApiKeyFilter filter = new InternalApiKeyFilter(API_KEY, entryPoint);
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest reqPublica = new MockHttpServletRequest("GET", "/api/transactions/ventas");
        MockHttpServletResponse resPublica = new MockHttpServletResponse();

        MockHttpServletRequest reqOk = new MockHttpServletRequest("POST", "/internal/test");
        reqOk.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, API_KEY);
        MockHttpServletResponse resOk = new MockHttpServletResponse();

        MockHttpServletRequest reqFail = new MockHttpServletRequest("POST", "/internal/test");
        reqFail.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, "clave-erronea");
        MockHttpServletResponse resFail = new MockHttpServletResponse();

        MockHttpServletRequest reqSinHeader = new MockHttpServletRequest("POST", "/internal/test");
        MockHttpServletResponse resSinHeader = new MockHttpServletResponse();

        // Act
        filter.doFilter(reqPublica, resPublica, chain);
        filter.doFilter(reqOk, resOk, chain);
        var authExitosa = SecurityContextHolder.getContext().getAuthentication();
        filter.doFilter(reqFail, resFail, chain);
        filter.doFilter(reqSinHeader, resSinHeader, chain);

        // Assert
        assertNotNull(authExitosa);
        assertEquals(401, resFail.getStatus());
        assertEquals(401, resSinHeader.getStatus());
        assertTrue(resFail.getContentAsString().contains("X-Internal-Api-Key"));
        verify(chain, never()).doFilter(reqFail, resFail);
    }
}
