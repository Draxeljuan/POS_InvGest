package com.invengest.gateway.infrastructure.entry_points.advice;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class GatewayJsonErrorWriter {

    public Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.empty();
        }
        response.setStatusCode(status);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String path = exchange.getRequest().getURI().getPath();
        String jsonBody = construirJson(
                LocalDateTime.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message != null ? message : status.getReasonPhrase(),
                path != null ? path : "/"
        );

        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public String construirJson(String timestamp, int status, String error, String message, String path) {
        return String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                escaparJson(timestamp),
                status,
                escaparJson(error),
                escaparJson(message),
                escaparJson(path)
        );
    }

    private String escaparJson(String valor) {
        if (valor == null) {
            return "";
        }
        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
