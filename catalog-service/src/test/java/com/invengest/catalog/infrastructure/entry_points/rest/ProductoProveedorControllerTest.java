package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.ProductoProveedor;
import com.invengest.catalog.domain.usecase.ProductoProveedorUseCase;
import com.invengest.catalog.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoProveedorResponse;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.catalog.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.catalog.infrastructure.mapper.ProductoProveedorRestMapper;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductoProveedorController.class)
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class ProductoProveedorControllerTest {

    private static final String JWT_SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String JWT_ISSUER = "invengest-pos-auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoProveedorUseCase productoProveedorUseCase;
    @MockitoBean
    private ProductoProveedorRestMapper productoProveedorRestMapper;

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
    @DisplayName("Expone endpoints de asociación producto-proveedor respondiendo 200, 201 y 204")
    void exponeEndpointsProductoProveedor() throws Exception {
        // Arrange
        String tokenAdmin = generarTokenAdmin();
        ProductoProveedor pp = new ProductoProveedor("PROD-0001", 1, BigDecimal.TEN);
        when(productoProveedorUseCase.obtenerPorProducto("PROD-0001")).thenReturn(List.of(pp));
        when(productoProveedorUseCase.obtenerPorProveedor(1)).thenReturn(List.of(pp));
        when(productoProveedorUseCase.asociarOActualizar(any())).thenReturn(pp);
        when(productoProveedorRestMapper.toDomain(any())).thenReturn(pp);
        when(productoProveedorRestMapper.toResponse(pp)).thenReturn(new ProductoProveedorResponse("PROD-0001", 1, BigDecimal.TEN));

        // Act & Assert
        mockMvc.perform(get("/api/catalog/productos-proveedores/producto/PROD-0001").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/productos-proveedores/proveedor/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(post("/api/catalog/productos-proveedores")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProducto\":\"PROD-0001\",\"idProveedor\":1,\"precioCompraProveedor\":4500.00}"))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/catalog/productos-proveedores/producto/PROD-0001/proveedor/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isNoContent());
    }
}
