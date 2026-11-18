package com.invengest.analytics.infrastructure.entry_points.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class InternalApiKeyFilterTest {

    private static final String INTERNAL_KEY = "internal_secret_key_2026";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe autorizar ROLE_SERVICE en rutas internas con X-Internal-Api-Key válida y dejar pasar rutas públicas")
    void doFilterInternal_RutaInternaValidaYRutaExterna() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        InternalApiKeyFilter filter = new InternalApiKeyFilter(INTERNAL_KEY, entryPoint);

        MockHttpServletRequest reqValida = new MockHttpServletRequest("POST", "/internal/ventas");
        reqValida.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, INTERNAL_KEY);
        MockHttpServletResponse resValida = new MockHttpServletResponse();
        FilterChain chainValida = mock(FilterChain.class);

        MockHttpServletRequest reqAlias = new MockHttpServletRequest("POST", "/api/analytics/internal/ventas");
        reqAlias.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, INTERNAL_KEY);

        MockHttpServletRequest reqExterna = new MockHttpServletRequest("GET", "/api/analytics/dashboard");
        FilterChain chainExterna = mock(FilterChain.class);

        // Act
        filter.doFilter(reqValida, resValida, chainValida);
        filter.doFilter(reqAlias, resValida, chainValida);
        filter.doFilter(reqExterna, resValida, chainExterna);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly(InternalApiKeyFilter.AUTHORITY_INTERNAL_SERVICE);
        verify(chainExterna).doFilter(reqExterna, resValida);
    }

    @Test
    @DisplayName("Debe responder 401 cuando la llave interna es inválida, ausente o no configurada")
    void doFilterInternal_LlaveInvalidaOAusente_Retorna401() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        InternalApiKeyFilter filter = new InternalApiKeyFilter(INTERNAL_KEY, entryPoint);
        InternalApiKeyFilter filterSinExpected = new InternalApiKeyFilter(" ", entryPoint);

        MockHttpServletRequest reqInvalida = new MockHttpServletRequest("POST", "/internal/alertas-stock");
        reqInvalida.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, "llave_falsa");
        MockHttpServletResponse resInvalida = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest reqAusente = new MockHttpServletRequest("POST", "/internal/alertas-stock");
        MockHttpServletResponse resAusente = new MockHttpServletResponse();

        MockHttpServletRequest reqBlanca = new MockHttpServletRequest("POST", "/internal/alertas-stock");
        reqBlanca.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, " ");
        MockHttpServletResponse resBlanca = new MockHttpServletResponse();

        MockHttpServletResponse resSinExpected = new MockHttpServletResponse();

        // Act
        filter.doFilter(reqInvalida, resInvalida, chain);
        filter.doFilter(reqAusente, resAusente, chain);
        filter.doFilter(reqBlanca, resBlanca, chain);
        filterSinExpected.doFilter(reqInvalida, resSinExpected, chain);

        // Assert
        assertThat(resInvalida.getStatus()).isEqualTo(401);
        assertThat(resAusente.getStatus()).isEqualTo(401);
        assertThat(resBlanca.getStatus()).isEqualTo(401);
        assertThat(resSinExpected.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(reqInvalida, resInvalida);
    }
}
