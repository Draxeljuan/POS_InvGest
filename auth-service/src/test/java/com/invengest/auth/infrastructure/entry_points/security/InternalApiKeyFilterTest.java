package com.invengest.auth.infrastructure.entry_points.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class InternalApiKeyFilterTest {

    private static final String API_KEY = "internal-secret-key-2026";

    private InternalApiKeyFilter internalApiKeyFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        internalApiKeyFilter = new InternalApiKeyFilter(API_KEY, new JsonAuthenticationEntryPoint());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe continuar para rutas no internas y rechazar con 401 si falta cabecera en ruta interna")
    void doFilterInternal_CuandoRutaNoInternaOSinHeader_DebeComportarseSegunRuta() throws Exception {
        // Arrange
        MockHttpServletRequest publicReq = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletRequest missingHeaderReq = new MockHttpServletRequest("GET", "/internal/usuarios/1/contacto");
        MockHttpServletResponse publicRes = new MockHttpServletResponse();
        MockHttpServletResponse missingRes = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        internalApiKeyFilter.doFilterInternal(publicReq, publicRes, chain);
        internalApiKeyFilter.doFilterInternal(missingHeaderReq, missingRes, chain);

        // Assert
        assertEquals(200, publicRes.getStatus());
        assertEquals(401, missingRes.getStatus());
        verify(chain, times(1)).doFilter(publicReq, publicRes);
    }

    @Test
    @DisplayName("Debe autenticar con ROLE_SERVICE cuando la llave interna es válida")
    void doFilterInternal_CuandoLlaveValidaEnRutaInterna_DebeAutenticarServicio() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/internal/usuarios/1/contacto");
        request.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, API_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        internalApiKeyFilter.doFilterInternal(request, response, chain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(InternalApiKeyFilter.AUTHORITY_INTERNAL_SERVICE)));
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Debe responder 401 JSON cuando la llave X-Internal-Api-Key es incorrecta")
    void doFilterInternal_CuandoLlaveIncorrecta_DebeResponder401Json() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/internal/usuarios/1/contacto");
        request.addHeader(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, "wrong-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        internalApiKeyFilter.doFilterInternal(request, response, chain);

        // Assert
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"status\":401"));
        verify(chain, never()).doFilter(request, response);
    }
}
