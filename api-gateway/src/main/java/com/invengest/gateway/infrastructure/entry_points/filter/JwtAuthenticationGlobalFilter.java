package com.invengest.gateway.infrastructure.entry_points.filter;

import com.invengest.gateway.domain.exception.AccesoRutaInternaProhibidoException;
import com.invengest.gateway.domain.exception.TokenInvalidoException;
import com.invengest.gateway.domain.usecase.EvaluarAccesoGatewayUseCase;
import com.invengest.gateway.infrastructure.entry_points.advice.GatewayJsonErrorWriter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    public static final int FILTER_ORDER = -50;

    private final EvaluarAccesoGatewayUseCase evaluarAccesoGatewayUseCase;
    private final GatewayJsonErrorWriter errorWriter;

    public JwtAuthenticationGlobalFilter(
            EvaluarAccesoGatewayUseCase evaluarAccesoGatewayUseCase,
            GatewayJsonErrorWriter errorWriter) {
        this.evaluarAccesoGatewayUseCase = evaluarAccesoGatewayUseCase;
        this.errorWriter = errorWriter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod httpMethod = request.getMethod();
        String method = httpMethod != null ? httpMethod.name() : "GET";
        String rawPath = request.getURI().getRawPath();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        try {
            evaluarAccesoGatewayUseCase.evaluarSolicitud(method, rawPath, authHeader);
            return chain.filter(exchange);
        } catch (AccesoRutaInternaProhibidoException ex) {
            return errorWriter.writeError(exchange, HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (TokenInvalidoException ex) {
            return errorWriter.writeError(exchange, HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }
}
