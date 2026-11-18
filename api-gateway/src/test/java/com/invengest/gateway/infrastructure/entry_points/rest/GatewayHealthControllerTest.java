package com.invengest.gateway.infrastructure.entry_points.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GatewayHealthControllerTest {

    @Test
    @DisplayName("Debe responder 200 OK con estado UP para healthcheck del Gateway")
    void health_DebeRetornarEstadoUp() {
        // Arrange
        GatewayHealthController controller = new GatewayHealthController();

        // Act
        ResponseEntity<Map<String, String>> response = controller.health().block();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("api-gateway", response.getBody().get("service"));
    }
}
