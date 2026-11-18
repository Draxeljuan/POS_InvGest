package com.invengest.gateway.infrastructure.entry_points.advice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayJsonErrorWriterTest {

    private GatewayJsonErrorWriter errorWriter;

    @BeforeEach
    void setUp() {
        errorWriter = new GatewayJsonErrorWriter();
    }

    @Test
    @DisplayName("Debe escribir respuesta JSON con messages y path homologados")
    void writeError_CuandoRespuestaNoComprometida_DebeEscribirJsonHomologado() {
        // Arrange
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/catalog/productos").build()
        );

        // Act
        errorWriter.writeError(exchange, HttpStatus.UNAUTHORIZED, "Token inválido").block();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, exchange.getResponse().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        String body = exchange.getResponse().getBodyAsString().block();
        assertNotNull(body);
        assertTrue(body.contains("\"status\":401"));
        assertTrue(body.contains("\"messages\":[\"Token inválido\"]"));
        assertTrue(body.contains("\"/api/catalog/productos\""));
    }

    @Test
    @DisplayName("Debe escapar caracteres especiales y manejar valores nulos en construirJson")
    void construirJson_CuandoValoresNulosOConCaracteresEspeciales_DebeEscaparCorrectamente() {
        // Arrange
        String mensajeConComillas = "Error \"crítico\"\nen línea\\1";

        // Act
        String jsonNulos = errorWriter.construirJson(null, 400, null, null, null);
        String jsonEscapado = errorWriter.construirJson("2026-11-17T22:00:00", 400, "Bad Request", mensajeConComillas, "/api/test");

        // Assert
        assertEquals("{\"timestamp\":\"\",\"status\":400,\"error\":\"\",\"messages\":[\"\"],\"message\":\"\",\"path\":\"\"}", jsonNulos);
        assertTrue(jsonEscapado.contains("Error \\\"crítico\\\" en línea\\\\1"));
    }
}
