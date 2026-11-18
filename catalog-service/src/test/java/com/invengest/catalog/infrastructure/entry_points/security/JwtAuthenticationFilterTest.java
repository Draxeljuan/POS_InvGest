package com.invengest.catalog.infrastructure.entry_points.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test_secret_key_for_catalog_service_minimum_32_bytes_2026";
    private static final String ISSUER = "invengest-pos-auth";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Autentica token JWT válido y limpia contexto en token inválido o sin cabecera")
    void autenticaTokenValidoYLimpaContextoEnTokenInvalido() throws Exception {
        // Arrange
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET, ISSUER);
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String validToken = Jwts.builder()
                .subject("admin")
                .issuer(ISSUER)
                .claim("role", "Administrador")
                .claim("userId", 1)
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        MockHttpServletRequest requestOk = new MockHttpServletRequest("GET", "/api/catalog/productos");
        requestOk.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + validToken);
        MockHttpServletResponse responseOk = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(requestOk, responseOk, chain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin", SecurityContextHolder.getContext().getAuthentication().getName());

        SecurityContextHolder.clearContext();
        MockHttpServletRequest requestBad = new MockHttpServletRequest("GET", "/api/catalog/productos");
        requestBad.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.manipulado");
        filter.doFilter(requestBad, new MockHttpServletResponse(), chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        MockHttpServletRequest requestNoAuth = new MockHttpServletRequest("GET", "/api/catalog/productos");
        filter.doFilter(requestNoAuth, new MockHttpServletResponse(), chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
