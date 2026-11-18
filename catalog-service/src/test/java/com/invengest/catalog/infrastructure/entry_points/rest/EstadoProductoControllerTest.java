package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.usecase.EstadoProductoUseCase;
import com.invengest.catalog.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.catalog.infrastructure.entry_points.dto.response.EstadoProductoResponse;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.catalog.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.catalog.infrastructure.mapper.EstadoProductoRestMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EstadoProductoController.class)
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class EstadoProductoControllerTest {

    private static final String JWT_SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String JWT_ISSUER = "invengest-pos-auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstadoProductoUseCase estadoProductoUseCase;
    @MockitoBean
    private EstadoProductoRestMapper estadoProductoRestMapper;

    private String generarTokenAdmin() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return "Bearer " + Jwts.builder()
                .subject("admin")
                .issuer(JWT_ISSUER)
                .claim("role", "Administrador")
                .claim("userId", 1)
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Consulta estados de producto por listado, ID y nombre")
    void consultaEstadosDeProducto() throws Exception {
        // Arrange
        String tokenAdmin = generarTokenAdmin();
        EstadoProducto ep = new EstadoProducto(1, "Activo");
        when(estadoProductoUseCase.obtenerTodos()).thenReturn(List.of(ep));
        when(estadoProductoUseCase.obtenerPorId(1)).thenReturn(ep);
        when(estadoProductoUseCase.obtenerPorNombre("Activo")).thenReturn(ep);
        when(estadoProductoRestMapper.toResponse(ep)).thenReturn(new EstadoProductoResponse(1, "Activo"));

        // Act & Assert
        mockMvc.perform(get("/api/catalog/estados-producto").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/estados-producto/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/estados-producto/nombre/Activo").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
    }
}
