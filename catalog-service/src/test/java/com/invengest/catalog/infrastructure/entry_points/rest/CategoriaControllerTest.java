package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.usecase.CategoriaUseCase;
import com.invengest.catalog.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.catalog.infrastructure.entry_points.dto.response.CategoriaResponse;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.catalog.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.catalog.infrastructure.mapper.CategoriaRestMapper;
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

@WebMvcTest(controllers = CategoriaController.class)
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class CategoriaControllerTest {

    private static final String JWT_SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String JWT_ISSUER = "invengest-pos-auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaUseCase categoriaUseCase;
    @MockitoBean
    private CategoriaRestMapper categoriaRestMapper;

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
    @DisplayName("Expone endpoints CRUD de categorías respondiendo 200, 201 y 204")
    void exponeEndpointsCrudDeCategorias() throws Exception {
        // Arrange
        String tokenAdmin = generarTokenAdmin();
        Categoria cat = new Categoria(1, "Escolar", "Útiles");
        when(categoriaUseCase.obtenerTodas()).thenReturn(List.of(cat));
        when(categoriaUseCase.obtenerPorId(1)).thenReturn(cat);
        when(categoriaUseCase.crear(any())).thenReturn(cat);
        when(categoriaUseCase.actualizar(any())).thenReturn(cat);
        when(categoriaRestMapper.toDomain(any())).thenReturn(cat);
        when(categoriaRestMapper.toResponse(cat)).thenReturn(new CategoriaResponse(1, "Escolar", "Útiles"));

        // Act & Assert
        mockMvc.perform(get("/api/catalog/categorias").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/categorias/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(post("/api/catalog/categorias")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Escolar\",\"descripcion\":\"Útiles\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/catalog/categorias/1")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Escolar\",\"descripcion\":\"Útiles\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/catalog/categorias/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isNoContent());
    }
}
