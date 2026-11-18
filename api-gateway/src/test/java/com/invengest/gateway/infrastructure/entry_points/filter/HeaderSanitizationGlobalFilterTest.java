package com.invengest.gateway.infrastructure.entry_points.filter;

import com.invengest.gateway.domain.usecase.EvaluarAccesoGatewayUseCase;
import com.invengest.gateway.infrastructure.driver_adapters.security.JwtTokenValidatorAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HeaderSanitizationGlobalFilterTest {

    private static final String SECRET = "clave_secreta_de_pruebas_unitarias_gateway_256_bits_2026";
    private static final String ISSUER = "invengest-pos-auth";

    private HeaderSanitizationGlobalFilter sanitizationFilter;

    @BeforeEach
    void setUp() {
        JwtTokenValidatorAdapter validatorAdapter = new JwtTokenValidatorAdapter(SECRET, ISSUER);
        EvaluarAccesoGatewayUseCase useCase = new EvaluarAccesoGatewayUseCase(validatorAdapter);
        sanitizationFilter = new HeaderSanitizationGlobalFilter(useCase);
    }

    @Test
    @DisplayName("Debe eliminar cabeceras X-User-* y X-Internal-Api-Key conservando Authorization")
    void filter_CuandoVienenCabecerasSensibles_DebeEliminarlasYConservarAuthorization() {
        // Arrange
        String bearer = "Bearer jwt-token-demo";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/catalog/productos")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .header("X-User-Id", "999")
                .header("X-User-Role", "Administrador")
                .header("X-User-Name", "spoofed")
                .header("x-user-custom", "hack")
                .header("X-Internal-Api-Key", "leaked-key")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        WebFilterChain chain = ex -> {
            capturedExchange.set(ex);
            return Mono.empty();
        };

        // Act
        sanitizationFilter.filter(exchange, chain).block();

        // Assert
        assertEquals(-100, sanitizationFilter.getOrder());
        ServerWebExchange forwarded = capturedExchange.get();
        assertNotNull(forwarded);
        HttpHeaders forwardedHeaders = forwarded.getRequest().getHeaders();
        assertEquals(bearer, forwardedHeaders.getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, forwardedHeaders.getFirst(HttpHeaders.ACCEPT));
        assertNull(forwardedHeaders.getFirst("X-User-Id"));
        assertNull(forwardedHeaders.getFirst("X-User-Role"));
        assertNull(forwardedHeaders.getFirst("X-User-Name"));
        assertNull(forwardedHeaders.getFirst("x-user-custom"));
        assertNull(forwardedHeaders.getFirst("X-Internal-Api-Key"));
    }
}
