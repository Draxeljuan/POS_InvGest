package com.invengest.gateway.infrastructure.entry_points.advice;

import com.invengest.gateway.domain.exception.AccesoRutaInternaProhibidoException;
import com.invengest.gateway.domain.exception.TokenInvalidoException;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

@Component
@Order(-2)
public class GlobalGatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final GatewayJsonErrorWriter errorWriter;

    public GlobalGatewayExceptionHandler(GatewayJsonErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof AccesoRutaInternaProhibidoException) {
            return errorWriter.writeError(exchange, HttpStatus.FORBIDDEN, ex.getMessage());
        }
        if (ex instanceof TokenInvalidoException) {
            return errorWriter.writeError(exchange, HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
        if (ex instanceof ResponseStatusException rse) {
            HttpStatusCode statusCode = rse.getStatusCode();
            HttpStatus resolved = HttpStatus.resolve(statusCode.value());
            HttpStatus finalStatus = resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
            String mensaje = rse.getReason() != null && !rse.getReason().isBlank()
                    ? rse.getReason()
                    : finalStatus == HttpStatus.NOT_FOUND
                    ? "La ruta solicitada no existe en el API Gateway"
                    : finalStatus.getReasonPhrase();
            return errorWriter.writeError(exchange, finalStatus, mensaje);
        }
        if (ex instanceof ConnectException || ex instanceof TimeoutException) {
            return errorWriter.writeError(
                    exchange,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El microservicio destino no está disponible temporalmente o excedió el tiempo de respuesta"
            );
        }
        return errorWriter.writeError(
                exchange,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno en el API Gateway al procesar la solicitud"
        );
    }
}
