package com.invengest.analytics;

import com.invengest.analytics.infrastructure.entry_points.security.InternalApiKeyFilter;
import com.invengest.analytics.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.analytics.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.analytics.infrastructure.entry_points.security.JwtAuthenticationFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SecurityFiltersAndHandlersTest {

    private static final String SECRET = "test_secret_key_for_analytics_service_minimum_32_bytes_2026";
    private static final String ISSUER = "invengest-pos-auth";
    private static final String INTERNAL_KEY = "internal_secret_key_2026";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("JwtAuthenticationFilter debe autenticar tokens válidos y limpiar contexto ante tokens inválidos")
    void jwtFilter_TokenValidoEInvalido() throws Exception {
        // Arrange
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET, ISSUER);
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String validToken = Jwts.builder()
                .subject("admin")
                .issuer(ISSUER)
                .claim("role", "Administrador")
                .claim("userId", 1)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        MockHttpServletRequest requestOk = new MockHttpServletRequest("GET", "/api/analytics/dashboard");
        requestOk.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + validToken);
        MockHttpServletResponse responseOk = new MockHttpServletResponse();
        FilterChain chainOk = mock(FilterChain.class);

        // Act
        filter.doFilter(requestOk, responseOk, chainOk);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin");
        verify(chainOk).doFilter(requestOk, responseOk);

        // Arrange (token corrupto)
        SecurityContextHolder.clearContext();
        MockHttpServletRequest requestBad = new MockHttpServletRequest("GET", "/api/analytics/dashboard");
        requestBad.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.firma");
        MockHttpServletResponse responseBad = new MockHttpServletResponse();
        FilterChain chainBad = mock(FilterChain.class);

        // Act
        filter.doFilter(requestBad, responseBad, chainBad);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chainBad).doFilter(requestBad, responseBad);
    }

    @Test
    @DisplayName("InternalApiKeyFilter debe autorizar ROLE_SERVICE con X-Internal-Api-Key válida y rechazar 401 si falta")
    void internalApiKeyFilter_ValidaYRechaza() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        InternalApiKeyFilter filter = new InternalApiKeyFilter(INTERNAL_KEY, entryPoint);

        MockHttpServletRequest reqValida = new MockHttpServletRequest("POST", "/internal/ventas");
        reqValida.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, INTERNAL_KEY);
        MockHttpServletResponse resValida = new MockHttpServletResponse();
        FilterChain chainValida = mock(FilterChain.class);

        // Act
        filter.doFilter(reqValida, resValida, chainValida);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly(InternalApiKeyFilter.AUTHORITY_INTERNAL_SERVICE);
        verify(chainValida).doFilter(reqValida, resValida);

        // Arrange (llave inválida en ruta /internal/)
        SecurityContextHolder.clearContext();
        MockHttpServletRequest reqInvalida = new MockHttpServletRequest("POST", "/internal/alertas-stock");
        reqInvalida.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, "llave_falsa");
        MockHttpServletResponse resInvalida = new MockHttpServletResponse();
        FilterChain chainInvalida = mock(FilterChain.class);

        // Act
        filter.doFilter(reqInvalida, resInvalida, chainInvalida);

        // Assert
        assertThat(resInvalida.getStatus()).isEqualTo(401);
        assertThat(resInvalida.getContentAsString()).contains("X-Internal-Api-Key");
        verify(chainInvalida, never()).doFilter(reqInvalida, resInvalida);
    }

    @Test
    @DisplayName("JsonAuthenticationEntryPoint y JsonAccessDeniedHandler deben escribir respuestas 401 y 403 en JSON")
    void handlers_EscribenJsonEstandar() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        JsonAccessDeniedHandler accessDeniedHandler = new JsonAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response401 = new MockHttpServletResponse();
        MockHttpServletResponse response403 = new MockHttpServletResponse();

        // Act
        entryPoint.commence(request, response401, new BadCredentialsException("No auth"));
        accessDeniedHandler.handle(request, response403, new AccessDeniedException("Forbidden"));

        // Assert
        assertThat(response401.getStatus()).isEqualTo(401);
        assertThat(response401.getContentAsString()).contains("\"status\":401");
        assertThat(response403.getStatus()).isEqualTo(403);
        assertThat(response403.getContentAsString()).contains("\"status\":403");
    }
}
