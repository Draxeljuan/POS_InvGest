package com.invengest.gateway;

import com.invengest.gateway.domain.exception.AccesoRutaInternaProhibidoException;
import com.invengest.gateway.domain.exception.TokenInvalidoException;
import com.invengest.gateway.domain.usecase.EvaluarAccesoGatewayUseCase;
import com.invengest.gateway.infrastructure.driver_adapters.security.JwtTokenValidatorAdapter;
import com.invengest.gateway.infrastructure.entry_points.advice.GatewayJsonErrorWriter;
import com.invengest.gateway.infrastructure.entry_points.advice.GlobalGatewayExceptionHandler;
import com.invengest.gateway.infrastructure.entry_points.filter.HeaderSanitizationGlobalFilter;
import com.invengest.gateway.infrastructure.entry_points.filter.JwtAuthenticationGlobalFilter;
import com.invengest.gateway.infrastructure.entry_points.rest.GatewayHealthController;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayFiltersAndErrorHandlingTest {

    private static final String SECRET = "clave_secreta_de_pruebas_unitarias_gateway_256_bits_2026";
    private static final String ISSUER = "invengest-pos-auth";

    private HeaderSanitizationGlobalFilter sanitizationFilter;
    private JwtAuthenticationGlobalFilter jwtAuthFilter;
    private GatewayJsonErrorWriter errorWriter;
    private GlobalGatewayExceptionHandler exceptionHandler;
    private GatewayHealthController healthController;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        JwtTokenValidatorAdapter validatorAdapter = new JwtTokenValidatorAdapter(SECRET, ISSUER);
        EvaluarAccesoGatewayUseCase useCase = new EvaluarAccesoGatewayUseCase(validatorAdapter);
        errorWriter = new GatewayJsonErrorWriter();
        sanitizationFilter = new HeaderSanitizationGlobalFilter(useCase);
        jwtAuthFilter = new JwtAuthenticationGlobalFilter(useCase, errorWriter);
        exceptionHandler = new GlobalGatewayExceptionHandler(errorWriter);
        healthController = new GatewayHealthController();
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
    void headerSanitizationFilter_ShouldStripXUserAndInternalApiKeyHeadersAndPreserveAuthorization() {
        // Arrange
        String bearer = "Bearer " + generarTokenValido();
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
        GatewayFilterChain chain = ex -> {
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

    @Test
    void jwtAuthenticationFilter_WhenValidTokenOnProtectedRoute_ShouldAllowAndPropagateAuthorization() {
        // Arrange
        String bearer = "Bearer " + generarTokenValido();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/transactions/ventas")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
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
    void jwtAuthenticationFilter_WhenPublicRouteWithoutToken_ShouldAllowRequestAndHealthCheckShouldReturnUp() {
        // Arrange
        MockServerHttpRequest loginReq = MockServerHttpRequest.post("/api/auth/login").build();
        MockServerHttpRequest registerReq = MockServerHttpRequest.post("/api/auth/register").build();
        MockServerWebExchange loginExchange = MockServerWebExchange.from(loginReq);
        MockServerWebExchange registerExchange = MockServerWebExchange.from(registerReq);

        AtomicReference<Boolean> loginAllowed = new AtomicReference<>(false);
        AtomicReference<Boolean> registerAllowed = new AtomicReference<>(false);

        // Act
        jwtAuthFilter.filter(loginExchange, ex -> {
            loginAllowed.set(true);
            return Mono.empty();
        }).block();

        jwtAuthFilter.filter(registerExchange, ex -> {
            registerAllowed.set(true);
            return Mono.empty();
        }).block();

        ResponseEntity<Map<String, String>> healthRes = healthController.health().block();

        // Assert
        assertTrue(loginAllowed.get());
        assertTrue(registerAllowed.get());
        assertNotNull(healthRes);
        assertEquals(HttpStatus.OK, healthRes.getStatusCode());
        assertEquals("UP", healthRes.getBody().get("status"));
    }

    @Test
    void jwtAuthenticationFilter_WhenMissingOrInvalidToken_ShouldReturn401Json() {
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
        assertTrue(body.contains("\"/api/catalog/productos\""));
    }

    @Test
    void jwtAuthenticationFilter_WhenInternalRouteRequested_ShouldBlockWith403Json() {
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

    @Test
    void globalGatewayExceptionHandler_ShouldFormatAllExceptionTypesAsStandardJson() {
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
        exceptionHandler.handle(ex400, new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud malformada\ncon comillas \"test\"")).block();
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
        assertEquals("{\"timestamp\":\"\",\"status\":400,\"error\":\"\",\"message\":\"\",\"path\":\"\"}",
                errorWriter.construirJson(null, 400, null, null, null));
    }
}
