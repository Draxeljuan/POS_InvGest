package com.invengest.gateway.infrastructure.entry_points.advice;

import com.invengest.gateway.domain.exception.AccesoRutaInternaProhibidoException;
import com.invengest.gateway.domain.exception.TokenInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalGatewayExceptionHandlerTest {

    private GlobalGatewayExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        GatewayJsonErrorWriter errorWriter = new GatewayJsonErrorWriter();
        exceptionHandler = new GlobalGatewayExceptionHandler(errorWriter);
    }

    @Test
    @DisplayName("Debe traducir excepciones de dominio, estado, red y genéricas a códigos HTTP estándar")
    void handle_CuandoOcurrenDistintasExcepciones_DebeResponderJsonConEstadoAdecuado() {
        // Arrange
        MockServerWebExchange ex403 = MockServerWebExchange.from(MockServerHttpRequest.get("/internal/ventas").build());
        MockServerWebExchange ex401 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/me").build());
        MockServerWebExchange ex404 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/unknown").build());
        MockServerWebExchange ex400 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/bad").build());
        MockServerWebExchange ex503Connect = MockServerWebExchange.from(MockServerHttpRequest.get("/api/catalog/productos").build());
        MockServerWebExchange ex503Timeout = MockServerWebExchange.from(MockServerHttpRequest.get("/api/catalog/productos").build());
        MockServerWebExchange ex500 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/error").build());

        // Act
        exceptionHandler.handle(ex403, new AccesoRutaInternaProhibidoException("Prohibido")).block();
        exceptionHandler.handle(ex401, new TokenInvalidoException("No autorizado")).block();
        exceptionHandler.handle(ex404, new ResponseStatusException(HttpStatus.NOT_FOUND)).block();
        exceptionHandler.handle(ex400, new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud malformada")).block();
        exceptionHandler.handle(ex503Connect, new ConnectException("Connection refused")).block();
        exceptionHandler.handle(ex503Timeout, new TimeoutException("Read timeout")).block();
        exceptionHandler.handle(ex500, new RuntimeException("Fallo inesperado")).block();

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, ex403.getResponse().getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, ex401.getResponse().getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, ex404.getResponse().getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex400.getResponse().getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex503Connect.getResponse().getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex503Timeout.getResponse().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex500.getResponse().getStatusCode());
    }
}
