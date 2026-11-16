package com.invengest.transaction;

import com.invengest.transaction.infrastructure.entry_points.security.InternalApiKeyFilter;
import com.invengest.transaction.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.transaction.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.transaction.infrastructure.entry_points.security.JwtAuthenticationFilter;
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

    private static final String SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String ISSUER = "invengest-pos-auth";
    private static final String API_KEY = "internal-key-2026";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtAuthenticationFilterAutenticaTokenValidoELimpiaContextoEnTokenInvalido() throws Exception {
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

        // Request sin header Authorization
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

    @Test
    void internalApiKeyFilterValidaRutasInternasYHandlersRetornanJson401Y403() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        JsonAccessDeniedHandler accessDeniedHandler = new JsonAccessDeniedHandler();
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

        MockHttpServletResponse res403 = new MockHttpServletResponse();

        // Act
        filter.doFilter(reqPublica, resPublica, chain);
        filter.doFilter(reqOk, resOk, chain);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        filter.doFilter(reqFail, resFail, chain);
        filter.doFilter(reqSinHeader, resSinHeader, chain);
        accessDeniedHandler.handle(reqFail, res403, new AccessDeniedException("Denegado"));
        entryPoint.commence(reqFail, new MockHttpServletResponse(), new BadCredentialsException("No auth"));

        // Assert
        assertEquals(401, resFail.getStatus());
        assertEquals(401, resSinHeader.getStatus());
        assertTrue(resFail.getContentAsString().contains("X-Internal-Api-Key"));
        assertEquals(403, res403.getStatus());
        assertTrue(res403.getContentAsString().contains("Acceso Denegado"));
        verify(chain, never()).doFilter(reqFail, resFail);
    }
}
