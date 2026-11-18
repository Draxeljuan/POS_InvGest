package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.usecase.CategoriaServicioUseCase;
import com.invengest.catalog.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.catalog.infrastructure.entry_points.dto.response.CategoriaServicioResponse;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.catalog.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.catalog.infrastructure.mapper.CategoriaServicioRestMapper;
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

@WebMvcTest(controllers = CategoriaServicioController.class)
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class CategoriaServicioControllerTest {

    private static final String JWT_SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String JWT_ISSUER = "invengest-pos-auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaServicioUseCase categoriaServicioUseCase;
    @MockitoBean
    private CategoriaServicioRestMapper categoriaServicioRestMapper;

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
    @DisplayName("Expone endpoints CRUD de categorías de servicio respondiendo 200, 201 y 204")
    void exponeEndpointsCrudDeCategoriasServicio() throws Exception {
        // Arrange
        String tokenAdmin = generarTokenAdmin();
        CategoriaServicio cs = new CategoriaServicio(1, "Impresión");
        when(categoriaServicioUseCase.obtenerTodas()).thenReturn(List.of(cs));
        when(categoriaServicioUseCase.obtenerPorId(1)).thenReturn(cs);
        when(categoriaServicioUseCase.crear(any())).thenReturn(cs);
        when(categoriaServicioUseCase.actualizar(any())).thenReturn(cs);
        when(categoriaServicioRestMapper.toDomain(any())).thenReturn(cs);
        when(categoriaServicioRestMapper.toResponse(cs)).thenReturn(new CategoriaServicioResponse(1, "Impresión"));

        // Act & Assert
        mockMvc.perform(get("/api/catalog/categorias-servicio").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/categorias-servicio/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(post("/api/catalog/categorias-servicio")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Impresión\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/catalog/categorias-servicio/1")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Impresión\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/catalog/categorias-servicio/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isNoContent());
    }
}
