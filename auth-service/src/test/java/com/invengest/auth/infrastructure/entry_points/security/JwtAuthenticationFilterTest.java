package com.invengest.auth.infrastructure.entry_points.security;

import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.infrastructure.driver_adapters.security.JwtProviderAdapter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test_secret_key_for_unit_testing_minimum_32_bytes_2026";
    private static final String ISSUER = "invengest-pos-auth";

    private JwtAuthenticationFilter jwtFilter;
    private JwtProviderAdapter jwtProvider;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtFilter = new JwtAuthenticationFilter(SECRET, ISSUER);
        jwtProvider = new JwtProviderAdapter(SECRET, 60000L, ISSUER);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe autenticar en SecurityContext cuando el token JWT es válido")
    void doFilterInternal_CuandoTokenValido_DebeAutenticar() throws Exception {
        // Arrange
        Usuario u = new Usuario(1, "Juan", "D", "j@m.com", "300", null,
                new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR), "juand", "h", true);
        String token = jwtProvider.generateToken(u);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        jwtFilter.doFilterInternal(request, response, chain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("juand", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Administrador")));
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Debe limpiar SecurityContext cuando el token es inválido o tiene otro issuer")
    void doFilterInternal_CuandoTokenInvalido_DebeLimpiarContexto() throws Exception {
        // Arrange
        JwtProviderAdapter wrongIssuerProvider = new JwtProviderAdapter(SECRET, 60000L, "other-issuer");
        Usuario u = new Usuario(1, "Juan", "D", "j@m.com", "300", null,
                new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR), "juand", "h", true);
        String tokenWrongIssuer = wrongIssuerProvider.generateToken(u);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWrongIssuer);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        jwtFilter.doFilterInternal(request, response, chain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Debe continuar sin autenticar cuando no hay cabecera Bearer")
    void doFilterInternal_CuandoSinHeaderONoBearer_DebeContinuarCadena() throws Exception {
        // Arrange
        MockHttpServletRequest reqNoHeader = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletRequest reqBasic = new MockHttpServletRequest("GET", "/api/auth/me");
        reqBasic.addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        jwtFilter.doFilterInternal(reqNoHeader, response, chain);
        jwtFilter.doFilterInternal(reqBasic, response, chain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, times(2)).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
