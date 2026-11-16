package com.invengest.gateway;

import com.invengest.gateway.domain.exception.AccesoRutaInternaProhibidoException;
import com.invengest.gateway.domain.exception.TokenInvalidoException;
import com.invengest.gateway.domain.model.DecisionAccesoRuta;
import com.invengest.gateway.domain.model.TokenClaims;
import com.invengest.gateway.domain.model.gateway.TokenValidatorGateway;
import com.invengest.gateway.domain.usecase.EvaluarAccesoGatewayUseCase;
import org.junit.jupiter.api.BeforeEach;
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
    void evaluarSolicitud_WhenRutaInterna_ShouldThrowAccesoRutaInternaProhibidoException() {
        // Arrange & Act & Assert
        assertThrows(AccesoRutaInternaProhibidoException.class, () ->
                useCase.evaluarSolicitud("POST", "/internal/ventas", "Bearer token-123"));
        assertThrows(AccesoRutaInternaProhibidoException.class, () ->
                useCase.evaluarSolicitud("POST", "/api/analytics/internal/alertas-stock", "Bearer token-123"));
        verifyNoInteractions(tokenValidatorGateway);
    }

    @Test
    void evaluarSolicitud_WhenRutaPublica_ShouldAllowWithoutToken() {
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
    void evaluarSolicitud_WhenRutaProtegidaConTokenValido_ShouldAuthorizeAndReturnClaims() {
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
    void evaluarSolicitud_WhenHeaderAuthorizationInvalido_ShouldThrowTokenInvalidoException() {
        // Arrange & Act & Assert
        assertThrows(TokenInvalidoException.class, () ->
                useCase.evaluarSolicitud("GET", "/api/catalog/productos", null));
        assertThrows(TokenInvalidoException.class, () ->
                useCase.evaluarSolicitud("GET", "/api/catalog/productos", "   "));
        assertThrows(TokenInvalidoException.class, () ->
                useCase.evaluarSolicitud("GET", "/api/catalog/productos", "Basic dXNlcjpwYXNz"));
        assertThrows(TokenInvalidoException.class, () ->
                useCase.evaluarSolicitud("GET", "/api/catalog/productos", "Bearer    "));
        verifyNoInteractions(tokenValidatorGateway);
    }

    @Test
    void identificarHeadersParaSanitizar_ShouldReturnOnlySensitiveHeaders() {
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
