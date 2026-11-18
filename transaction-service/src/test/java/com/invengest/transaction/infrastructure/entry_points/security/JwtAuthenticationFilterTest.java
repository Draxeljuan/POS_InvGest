package com.invengest.transaction.infrastructure.entry_points.security;

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
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String ISSUER = "invengest-pos-auth";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe autenticar token JWT válido y limpiar contexto ante token ausente o inválido")
    void debeAutenticarTokenValidoELimpiarEnTokenInvalido() throws Exception {
        // Arrange
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET, ISSUER);
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String tokenValido = Jwts.builder()
                .subject("vendedor1")
                .issuer(ISSUER)
                .claim("role", "Vendedor")
                .claim("userId", 5)
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        MockHttpServletRequest reqValida = new MockHttpServletRequest();
        reqValida.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido);
        MockHttpServletResponse resValida = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(reqValida, resValida, chain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("vendedor1", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(chain).doFilter(reqValida, resValida);

        // Sin header Authorization
        SecurityContextHolder.clearContext();
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Token inválido
        SecurityContextHolder.clearContext();
        MockHttpServletRequest reqInvalida = new MockHttpServletRequest();
        reqInvalida.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token.manipulado.invalido");
        filter.doFilter(reqInvalida, new MockHttpServletResponse(), chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
