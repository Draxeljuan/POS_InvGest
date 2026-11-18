package com.invengest.gateway.infrastructure.entry_points.filter;

import com.invengest.gateway.domain.usecase.EvaluarAccesoGatewayUseCase;
import com.invengest.gateway.infrastructure.driver_adapters.security.JwtTokenValidatorAdapter;
import com.invengest.gateway.infrastructure.entry_points.advice.GatewayJsonErrorWriter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationGlobalFilterTest {

    private static final String SECRET = "clave_secreta_de_pruebas_unitarias_gateway_256_bits_2026";
    private static final String ISSUER = "invengest-pos-auth";

    private JwtAuthenticationGlobalFilter jwtAuthFilter;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        JwtTokenValidatorAdapter validatorAdapter = new JwtTokenValidatorAdapter(SECRET, ISSUER);
        EvaluarAccesoGatewayUseCase useCase = new EvaluarAccesoGatewayUseCase(validatorAdapter);
        GatewayJsonErrorWriter errorWriter = new GatewayJsonErrorWriter();
        jwtAuthFilter = new JwtAuthenticationGlobalFilter(useCase, errorWriter);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String generarTokenValido() {
        Date now = new Date();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject("admin")
                .claim("role", "Administrador")
                .claim("userId", 1)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 60_000))
                .signWith(signingKey)
                .compact();
    }

    @Test
    @DisplayName("Debe permitir petición y propagar Authorization cuando el JWT es válido en ruta protegida")
    void filter_CuandoTokenValidoEnRutaProtegida_DebePermitir() {
        // Arrange
        String bearer = "Bearer " + generarTokenValido();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/transactions/ventas")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();
        WebFilterChain chain = ex -> {
            captured.set(ex);
            return Mono.empty();
        };

        // Act
        jwtAuthFilter.filter(exchange, chain).block();

        // Assert
        assertEquals(-50, jwtAuthFilter.getOrder());
        assertNotNull(captured.get());
        assertEquals(bearer, captured.get().getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    @DisplayName("Debe permitir rutas públicas sin requerir token")
    void filter_CuandoRutaPublicaSinToken_DebePermitir() {
        // Arrange
        MockServerHttpRequest loginReq = MockServerHttpRequest.post("/api/auth/login").build();
        MockServerWebExchange loginExchange = MockServerWebExchange.from(loginReq);
        AtomicReference<Boolean> loginAllowed = new AtomicReference<>(false);

        // Act
        jwtAuthFilter.filter(loginExchange, ex -> {
            loginAllowed.set(true);
            return Mono.empty();
        }).block();

        // Assert
        assertTrue(loginAllowed.get());
    }

    @Test
    @DisplayName("Debe responder 401 JSON cuando falta el token o es inválido")
    void filter_CuandoFaltaTokenOInvalido_DebeResponder401Json() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/catalog/productos").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<Boolean> chainCalled = new AtomicReference<>(false);

        // Act
        jwtAuthFilter.filter(exchange, ex -> {
            chainCalled.set(true);
            return Mono.empty();
        }).block();

        // Assert
        assertFalse(chainCalled.get());
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, exchange.getResponse().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        String body = exchange.getResponse().getBodyAsString().block();
        assertNotNull(body);
        assertTrue(body.contains("\"status\":401"));
        assertTrue(body.contains("\"messages\":"));
        assertTrue(body.contains("\"/api/catalog/productos\""));
    }

    @Test
    @DisplayName("Debe bloquear cualquier ruta /internal/** con 403 JSON uniforme antes de enrutar")
    void filter_CuandoRutaInterna_DebeBloquearCon403Json() {
        // Arrange
        String bearer = "Bearer " + generarTokenValido();
        MockServerHttpRequest request = MockServerHttpRequest.post("/internal/productos/descontar-stock")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        jwtAuthFilter.filter(exchange, ex -> Mono.empty()).block();

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        String body = exchange.getResponse().getBodyAsString().block();
        assertNotNull(body);
        assertTrue(body.contains("\"status\":403"));
        assertTrue(body.contains("rutas internas"));
    }
}
