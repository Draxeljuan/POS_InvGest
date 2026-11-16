package com.invengest.catalog;

import com.invengest.catalog.infrastructure.entry_points.security.InternalApiKeyFilter;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.catalog.infrastructure.entry_points.security.JwtAuthenticationFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SecurityFiltersAndHandlersTest {

    private static final String SECRET = "test_secret_key_for_catalog_service_minimum_32_bytes_2026";
    private static final String ISSUER = "invengest-pos-auth";
    private static final String INTERNAL_KEY = "internal-key-2026";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtFilterAutenticaTokenValidoYLimpaContextoEnTokenInvalido() throws Exception {
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
    }

    @Test
    void internalApiKeyFilterAutorizaLlaveValidaYRechaza401EnLlaveInvalida() throws Exception {
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

        // Act
        filter.doFilter(reqValida, resValida, chain);
        filter.doFilter(reqInvalida, resInvalida, chain);

        // Assert
        verify(chain).doFilter(reqValida, resValida);
        verify(chain, never()).doFilter(reqInvalida, resInvalida);
        assertEquals(401, resInvalida.getStatus());
        assertTrue(resInvalida.getContentAsString().contains("X-Internal-Api-Key"));
    }

    @Test
    void handlersRetornanJsonEstructuradoPara401Y403() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        JsonAccessDeniedHandler accessDeniedHandler = new JsonAccessDeniedHandler();
        MockHttpServletResponse res401 = new MockHttpServletResponse();
        MockHttpServletResponse res403 = new MockHttpServletResponse();

        // Act
        entryPoint.commence(new MockHttpServletRequest(), res401, new BadCredentialsException("Sin token"));
        accessDeniedHandler.handle(new MockHttpServletRequest(), res403, new AccessDeniedException("Prohibido"));

        // Assert
        assertEquals(401, res401.getStatus());
        assertTrue(res401.getContentAsString().contains("\"status\":401"));
        assertEquals(403, res403.getStatus());
        assertTrue(res403.getContentAsString().contains("\"status\":403"));
    }
}
