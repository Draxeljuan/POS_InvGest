package com.invengest.auth.infrastructure.entry_points.security;

import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.infrastructure.driver_adapters.security.JwtProviderAdapter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SecurityFiltersAndHandlersTest {

    private static final String SECRET = "test_secret_key_for_unit_testing_minimum_32_bytes_2026";
    private static final String ISSUER = "invengest-pos-auth";
    private static final String API_KEY = "internal-secret-key-2026";

    private JwtAuthenticationFilter jwtFilter;
    private JwtProviderAdapter jwtProvider;
    private InternalApiKeyFilter internalApiKeyFilter;
    private JsonAuthenticationEntryPoint entryPoint;
    private JsonAccessDeniedHandler accessDeniedHandler;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtFilter = new JwtAuthenticationFilter(SECRET, ISSUER);
        jwtProvider = new JwtProviderAdapter(SECRET, 60000L, ISSUER);
        entryPoint = new JsonAuthenticationEntryPoint();
        accessDeniedHandler = new JsonAccessDeniedHandler();
        internalApiKeyFilter = new InternalApiKeyFilter(API_KEY, entryPoint);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtFilter_ShouldAuthenticate_WhenTokenIsValid() throws Exception {
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
    void jwtFilter_ShouldClearContext_WhenTokenIsInvalidOrWrongIssuer() throws Exception {
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
    void jwtFilter_ShouldPassThrough_WhenNoHeaderOrNonBearer() throws Exception {
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

    @Test
    void internalApiKeyFilter_ShouldPassThrough_WhenNonInternalPathOrMissingHeader() throws Exception {
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
    void internalApiKeyFilter_ShouldAuthenticateService_WhenValidKeyOnInternalPath() throws Exception {
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
    void internalApiKeyFilter_ShouldReturn401Json_WhenKeyIsInvalidOrMissing() throws Exception {
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

    @Test
    void entryPointAndAccessDeniedHandler_ShouldWriteStandardJson() throws Exception {
        // Arrange
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res401 = new MockHttpServletResponse();
        MockHttpServletResponse res403 = new MockHttpServletResponse();

        // Act
        entryPoint.commence(req, res401, new BadCredentialsException("No token"));
        accessDeniedHandler.handle(req, res403, new AccessDeniedException("Forbidden"));

        // Assert
        assertEquals(401, res401.getStatus());
        assertTrue(res401.getContentAsString().contains("\"status\":401"));
        assertEquals(403, res403.getStatus());
        assertTrue(res403.getContentAsString().contains("\"status\":403"));
    }
}
