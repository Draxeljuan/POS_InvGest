package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.model.Servicio;
import com.invengest.catalog.domain.usecase.ServicioUseCase;
import com.invengest.catalog.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.catalog.infrastructure.entry_points.dto.response.CategoriaServicioResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ServicioResponse;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.catalog.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.catalog.infrastructure.mapper.ServicioRestMapper;
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
import java.math.BigDecimal;
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

@WebMvcTest(controllers = ServicioController.class)
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class ServicioControllerTest {

    private static final String JWT_SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String JWT_ISSUER = "invengest-pos-auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicioUseCase servicioUseCase;
    @MockitoBean
    private ServicioRestMapper servicioRestMapper;

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
    @DisplayName("Expone endpoints CRUD de servicios respondiendo 200, 201 y 204")
    void exponeEndpointsCrudDeServicios() throws Exception {
        // Arrange
        String tokenAdmin = generarTokenAdmin();
        CategoriaServicio cs = new CategoriaServicio(1, "Impresión");
        Servicio serv = new Servicio(1, cs, "Fotocopia", "B/N", new BigDecimal("200.00"));
        when(servicioUseCase.obtenerTodos()).thenReturn(List.of(serv));
        when(servicioUseCase.obtenerPorId(1)).thenReturn(serv);
        when(servicioUseCase.obtenerPorCategoria(1)).thenReturn(List.of(serv));
        when(servicioUseCase.registrar(any())).thenReturn(serv);
        when(servicioUseCase.actualizar(any())).thenReturn(serv);
        when(servicioRestMapper.toDomain(any())).thenReturn(serv);
        when(servicioRestMapper.toResponse(serv))
                .thenReturn(new ServicioResponse(1, new CategoriaServicioResponse(1, "Impresión"), "Fotocopia", "B/N", new BigDecimal("200.00")));

        // Act & Assert
        mockMvc.perform(get("/api/catalog/servicios").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/servicios/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/servicios/categoria/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(post("/api/catalog/servicios")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCategoriaServicio\":1,\"nombre\":\"Fotocopia\",\"precioSugerido\":200.00}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/catalog/servicios/1")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCategoriaServicio\":1,\"nombre\":\"Fotocopia\",\"precioSugerido\":200.00}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/catalog/servicios/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isNoContent());
    }
}
