package com.invengest.analytics.infrastructure.entry_points.security;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test_secret_key_for_analytics_service_minimum_32_bytes_2026";
    private static final String ISSUER = "invengest-pos-auth";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe autenticar tokens JWT válidos con y sin userId e ignorar cabeceras ausentes o sin prefijo Bearer")
    void doFilterInternal_TokenValidoYSinCabecera() throws Exception {
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
        String tokenSinUserId = Jwts.builder()
                .subject("vendedor")
                .issuer(ISSUER)
                .claim("role", "Vendedor")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        MockHttpServletRequest requestOk = new MockHttpServletRequest("GET", "/api/analytics/dashboard");
        requestOk.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + validToken);
        MockHttpServletResponse responseOk = new MockHttpServletResponse();
        FilterChain chainOk = mock(FilterChain.class);

        MockHttpServletRequest reqSinHeader = new MockHttpServletRequest("GET", "/api/analytics/dashboard");
        FilterChain chainSinHeader = mock(FilterChain.class);

        MockHttpServletRequest reqBasic = new MockHttpServletRequest("GET", "/api/analytics/dashboard");
        reqBasic.addHeader(HttpHeaders.AUTHORIZATION, "Basic 12345");
        FilterChain chainBasic = mock(FilterChain.class);

        MockHttpServletRequest reqSinUserId = new MockHttpServletRequest("GET", "/api/analytics/dashboard");
        reqSinUserId.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSinUserId);

        // Act
        filter.doFilter(requestOk, responseOk, chainOk);
        String authenticatedUser = SecurityContextHolder.getContext().getAuthentication().getName();
        filter.doFilter(requestOk, responseOk, chainOk);

        SecurityContextHolder.clearContext();
        filter.doFilter(reqSinHeader, responseOk, chainSinHeader);
        filter.doFilter(reqBasic, responseOk, chainBasic);
        filter.doFilter(reqSinUserId, responseOk, chainOk);

        // Assert
        assertThat(authenticatedUser).isEqualTo("admin");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("vendedor");
        verify(chainSinHeader).doFilter(reqSinHeader, responseOk);
        verify(chainBasic).doFilter(reqBasic, responseOk);
    }

    @Test
    @DisplayName("Debe limpiar SecurityContext cuando el token es corrupto o carece de subject o role")
    void doFilterInternal_TokenInvalidoOSinClaims() throws Exception {
        // Arrange
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET, ISSUER);
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String tokenSinRol = Jwts.builder()
                .subject("admin")
                .issuer(ISSUER)
                .claim("role", " ")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
        String tokenSinSubject = Jwts.builder()
                .issuer(ISSUER)
                .claim("role", "Administrador")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        MockHttpServletRequest reqCorrupto = new MockHttpServletRequest("GET", "/api/analytics/dashboard");
        reqCorrupto.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.firma");
        MockHttpServletRequest reqSinRol = new MockHttpServletRequest("GET", "/api/analytics/dashboard");
        reqSinRol.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSinRol);
        MockHttpServletRequest reqSinSubject = new MockHttpServletRequest("GET", "/api/analytics/dashboard");
        reqSinSubject.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenSinSubject);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(reqCorrupto, response, chain);
        filter.doFilter(reqSinRol, response, chain);
        filter.doFilter(reqSinSubject, response, chain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
