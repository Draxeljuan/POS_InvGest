package com.invengest.gateway.infrastructure.entry_points.filter;

import com.invengest.gateway.domain.model.PolizaSeguridadGateway;
import com.invengest.gateway.domain.usecase.EvaluarAccesoGatewayUseCase;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class HeaderSanitizationGlobalFilter implements GlobalFilter, Ordered {

    public static final int FILTER_ORDER = -100;

    private final EvaluarAccesoGatewayUseCase evaluarAccesoGatewayUseCase;

    public HeaderSanitizationGlobalFilter(EvaluarAccesoGatewayUseCase evaluarAccesoGatewayUseCase) {
        this.evaluarAccesoGatewayUseCase = evaluarAccesoGatewayUseCase;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        List<String> headersParaEliminar = evaluarAccesoGatewayUseCase.identificarHeadersParaSanitizar(
                request.getHeaders().headerNames()
        );

        ServerHttpRequest sanitizedRequest = request.mutate()
                .headers(httpHeaders -> {
                    httpHeaders.remove(PolizaSeguridadGateway.HEADER_INTERNAL_API_KEY);
                    httpHeaders.remove("X-User-Id");
                    httpHeaders.remove("X-User-Role");
                    httpHeaders.remove("X-User-Name");
                    for (String headerName : headersParaEliminar) {
                        httpHeaders.remove(headerName);
                    }
                })
                .build();

        return chain.filter(exchange.mutate().request(sanitizedRequest).build());
    }

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }
}
