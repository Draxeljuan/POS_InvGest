package com.invengest.gateway.domain.usecase;

import com.invengest.gateway.domain.exception.AccesoRutaInternaProhibidoException;
import com.invengest.gateway.domain.exception.TokenInvalidoException;
import com.invengest.gateway.domain.model.DecisionAccesoRuta;
import com.invengest.gateway.domain.model.PolizaSeguridadGateway;
import com.invengest.gateway.domain.model.TokenClaims;
import com.invengest.gateway.domain.model.gateway.TokenValidatorGateway;

import java.util.Collection;
import java.util.List;

public class EvaluarAccesoGatewayUseCase {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenValidatorGateway tokenValidatorGateway;

    public EvaluarAccesoGatewayUseCase(TokenValidatorGateway tokenValidatorGateway) {
        this.tokenValidatorGateway = tokenValidatorGateway;
    }

    public DecisionAccesoRuta evaluarSolicitud(String metodoHttp, String rawPath, String authorizationHeader) {
        if (PolizaSeguridadGateway.esRutaInternaBloqueada(rawPath)) {
            throw new AccesoRutaInternaProhibidoException(
                    "Acceso denegado: las rutas internas (/internal/**) son exclusivas para comunicación inter-servicios");
        }

        if (PolizaSeguridadGateway.esRutaPublica(metodoHttp, rawPath)) {
            return DecisionAccesoRuta.permitirRutaPublica();
        }

        String token = extraerTokenBearer(authorizationHeader);
        TokenClaims claims = tokenValidatorGateway.validarYExtraerClaims(token);
        claims.validarIdentidadCompleta();

        return DecisionAccesoRuta.autorizarRutaProtegida(claims);
    }

    public String extraerTokenBearer(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new TokenInvalidoException("Se requiere autenticación JWT (Header Authorization ausente)");
        }
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new TokenInvalidoException("Formato de Authorization inválido. Debe comenzar con 'Bearer '");
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new TokenInvalidoException("El token Bearer enviado está vacío");
        }
        return token;
    }

    public List<String> identificarHeadersParaSanitizar(Collection<String> nombresHeadersEntrantes) {
        if (nombresHeadersEntrantes == null || nombresHeadersEntrantes.isEmpty()) {
            return List.of();
        }
        return nombresHeadersEntrantes.stream()
                .filter(PolizaSeguridadGateway::esHeaderSensibleSanitizable)
                .toList();
    }
}
