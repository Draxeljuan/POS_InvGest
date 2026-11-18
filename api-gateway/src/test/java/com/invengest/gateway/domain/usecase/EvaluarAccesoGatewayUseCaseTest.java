package com.invengest.gateway.domain.usecase;

import com.invengest.gateway.domain.exception.AccesoRutaInternaProhibidoException;
import com.invengest.gateway.domain.exception.TokenInvalidoException;
import com.invengest.gateway.domain.model.DecisionAccesoRuta;
import com.invengest.gateway.domain.model.TokenClaims;
import com.invengest.gateway.domain.model.gateway.TokenValidatorGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluarAccesoGatewayUseCaseTest {

    @Mock
    private TokenValidatorGateway tokenValidatorGateway;

    private EvaluarAccesoGatewayUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new EvaluarAccesoGatewayUseCase(tokenValidatorGateway);
    }

    @Test
    @DisplayName("Debe lanzar AccesoRutaInternaProhibidoException cuando la ruta es interna")
    void evaluarSolicitud_CuandoRutaInterna_DebeLanzarExcepcion() {
        // Arrange
        String token = "Bearer token-123";

        // Act & Assert
        assertThrows(AccesoRutaInternaProhibidoException.class, () ->
                useCase.evaluarSolicitud("POST", "/internal/ventas", token));
        assertThrows(AccesoRutaInternaProhibidoException.class, () ->
                useCase.evaluarSolicitud("POST", "/api/analytics/internal/alertas-stock", token));
        verifyNoInteractions(tokenValidatorGateway);
    }

    @Test
    @DisplayName("Debe permitir rutas públicas sin validar token")
    void evaluarSolicitud_CuandoRutaPublica_DebePermitirSinToken() {
        // Arrange & Act
        DecisionAccesoRuta loginDecision = useCase.evaluarSolicitud("POST", "/api/auth/login", null);
        DecisionAccesoRuta registerDecision = useCase.evaluarSolicitud("POST", "/api/auth/register", null);
        DecisionAccesoRuta optionsDecision = useCase.evaluarSolicitud("OPTIONS", "/api/catalog/productos", null);

        // Assert
        assertTrue(loginDecision.rutaPublica());
        assertTrue(registerDecision.rutaPublica());
        assertTrue(optionsDecision.rutaPublica());
        verifyNoInteractions(tokenValidatorGateway);
    }

    @Test
    @DisplayName("Debe autorizar ruta protegida y retornar claims cuando el token es válido")
    void evaluarSolicitud_CuandoRutaProtegidaConTokenValido_DebeRetornarClaims() {
        // Arrange
        TokenClaims expectedClaims = new TokenClaims("admin", "Administrador", "1", "invengest-pos-auth");
        when(tokenValidatorGateway.validarYExtraerClaims("jwt-valido")).thenReturn(expectedClaims);

        // Act
        DecisionAccesoRuta decision = useCase.evaluarSolicitud("GET", "/api/catalog/productos", "Bearer jwt-valido");

        // Assert
        assertFalse(decision.rutaPublica());
        assertEquals(expectedClaims, decision.tokenClaims());
        verify(tokenValidatorGateway).validarYExtraerClaims("jwt-valido");
    }

    @Test
    @DisplayName("Debe lanzar TokenInvalidoException cuando el header Authorization es nulo o malformado")
    void evaluarSolicitud_CuandoHeaderAuthorizationInvalido_DebeLanzarExcepcion() {
        // Arrange
        String rutaProtegida = "/api/catalog/productos";

        // Act & Assert
        assertThrows(TokenInvalidoException.class, () ->
                useCase.evaluarSolicitud("GET", rutaProtegida, null));
        assertThrows(TokenInvalidoException.class, () ->
                useCase.evaluarSolicitud("GET", rutaProtegida, "   "));
        assertThrows(TokenInvalidoException.class, () ->
                useCase.evaluarSolicitud("GET", rutaProtegida, "Basic dXNlcjpwYXNz"));
        assertThrows(TokenInvalidoException.class, () ->
                useCase.evaluarSolicitud("GET", rutaProtegida, "Bearer    "));
        verifyNoInteractions(tokenValidatorGateway);
    }

    @Test
    @DisplayName("Debe identificar únicamente cabeceras sensibles para sanitizar")
    void identificarHeadersParaSanitizar_DebeRetornarSoloSensibles() {
        // Arrange
        List<String> entrantes = List.of("Authorization", "X-User-Role", "x-user-id", "X-Internal-Api-Key", "Accept");

        // Act
        List<String> sanitizables = useCase.identificarHeadersParaSanitizar(entrantes);
        List<String> vacios = useCase.identificarHeadersParaSanitizar(null);

        // Assert
        assertEquals(List.of("X-User-Role", "x-user-id", "X-Internal-Api-Key"), sanitizables);
        assertTrue(vacios.isEmpty());
    }
}
