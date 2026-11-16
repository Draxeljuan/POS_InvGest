package com.invengest.notification.infrastructure.entry_points.security;

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
import org.springframework.security.core.Authentication;
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

    private static final String SECRET = "test_secret_key_for_unit_testing_minimum_32_bytes_2026";
    private static final String ISSUER = "invengest-pos-auth";
    private static final String INTERNAL_KEY = "internal_test_key_2026";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("JwtAuthenticationFilter autentica correctamente un token HS256 válido")
    void jwtFilter_TokenValido_EstableceAutenticacion() throws Exception {
        // Arrange
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET, ISSUER);
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("admin")
                .issuer(ISSUER)
                .claim("role", "Administrador")
                .claim("userId", 1)
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("admin", auth.getPrincipal());
        assertEquals("1", auth.getCredentials());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Administrador")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("JwtAuthenticationFilter limpia contexto cuando el token es inválido")
    void jwtFilter_TokenManipulado_LimpiaContexto() throws Exception {
        // Arrange
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET, ISSUER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.manipulado");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("JwtAuthenticationFilter continúa sin autenticar cuando no hay header Bearer")
    void jwtFilter_SinHeader_ContinuaCadena() throws Exception {
        // Arrange
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET, ISSUER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("InternalApiKeyFilter autoriza petición a /internal/** con llave válida")
    void internalFilter_LlaveValida_EstableceRoleService() throws Exception {
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
    @DisplayName("InternalApiKeyFilter rechaza con 401 JSON cuando falta X-Internal-Api-Key en /internal/**")
    void internalFilter_LlaveInvalida_Retorna401Json() throws Exception {
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
    @DisplayName("InternalApiKeyFilter ignora rutas públicas o de /api/**")
    void internalFilter_RutaNoInterna_ContinuaCadena() throws Exception {
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

    @Test
    @DisplayName("JsonAuthenticationEntryPoint y JsonAccessDeniedHandler escriben respuestas JSON 401 y 403")
    void handlers_EscribenJson401Y403() throws Exception {
        // Arrange
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        JsonAccessDeniedHandler accessDeniedHandler = new JsonAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response401 = new MockHttpServletResponse();
        MockHttpServletResponse response403 = new MockHttpServletResponse();

        // Act
        entryPoint.commence(request, response401, null);
        accessDeniedHandler.handle(request, response403, new AccessDeniedException("Denegado"));

        // Assert
        assertEquals(401, response401.getStatus());
        assertTrue(response401.getContentAsString().contains("\"status\":401"));
        assertEquals(403, response403.getStatus());
        assertTrue(response403.getContentAsString().contains("\"status\":403"));
    }
}
