package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.domain.usecase.ProveedorUseCase;
import com.invengest.catalog.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.catalog.infrastructure.entry_points.dto.response.EstadoProveedorResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProveedorResponse;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.catalog.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.catalog.infrastructure.mapper.ProveedorRestMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProveedorController.class)
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class ProveedorControllerTest {

    private static final String JWT_SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String JWT_ISSUER = "invengest-pos-auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProveedorUseCase proveedorUseCase;
    @MockitoBean
    private ProveedorRestMapper proveedorRestMapper;

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
    @DisplayName("Expone endpoints CRUD de proveedores respondiendo 200, 201 y 204")
    void exponeEndpointsCrudDeProveedores() throws Exception {
        // Arrange
        String tokenAdmin = generarTokenAdmin();
        EstadoProveedor epr = new EstadoProveedor(1, "Activo");
        Proveedor prov = new Proveedor(1, epr, "Norma", "300", "a@a.com", "Dir", "900");
        when(proveedorUseCase.obtenerTodos()).thenReturn(List.of(prov));
        when(proveedorUseCase.obtenerPorId(1)).thenReturn(prov);
        when(proveedorUseCase.crear(any())).thenReturn(prov);
        when(proveedorUseCase.actualizar(any())).thenReturn(prov);
        when(proveedorRestMapper.toDomain(any())).thenReturn(prov);
        when(proveedorRestMapper.toResponse(prov))
                .thenReturn(new ProveedorResponse(1, new EstadoProveedorResponse(1, "Activo"), "Norma", "300", "a@a.com", "Dir", "900"));

        // Act & Assert
        mockMvc.perform(get("/api/catalog/proveedores").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/proveedores/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(post("/api/catalog/proveedores")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEstadoProveedor\":1,\"nombre\":\"Norma\",\"nit\":\"900\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/catalog/proveedores/1")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEstadoProveedor\":1,\"nombre\":\"Norma\",\"nit\":\"900\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/catalog/proveedores/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isNoContent());
    }
}
