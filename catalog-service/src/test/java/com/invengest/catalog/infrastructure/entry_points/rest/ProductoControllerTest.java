package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.MejorProveedorProducto;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.ProductoPrecioUsd;
import com.invengest.catalog.domain.model.TasaCambio;
import com.invengest.catalog.domain.usecase.ProductoUseCase;
import com.invengest.catalog.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.catalog.infrastructure.entry_points.dto.response.CategoriaResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.EstadoProductoResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.MejorProveedorResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoPrecioUsdResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoResponse;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.catalog.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.catalog.infrastructure.mapper.ProductoRestMapper;
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
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductoController.class)
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class ProductoControllerTest {

    private static final String JWT_SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String JWT_ISSUER = "invengest-pos-auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoUseCase productoUseCase;

    @MockitoBean
    private ProductoRestMapper productoRestMapper;

    private String generarToken(String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(username)
                .issuer(JWT_ISSUER)
                .claim("role", role)
                .claim("userId", 1)
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Permite a Vendedor consultar productos y precios USD pero bloquea mejor proveedor con 403")
    void endpointsDeConsultaProductosYPreciosUsdPermitenVendedorPeroMejorProveedorExigeAdmin() throws Exception {
        // Arrange
        String tokenVendedor = "Bearer " + generarToken("vendedor1", "Vendedor");
        Producto prod = new Producto("PROD-0001", new Categoria(1, "Cuadernos", ""), "Cuaderno",
                new BigDecimal("8000.00"), new BigDecimal("5000.00"), LocalDate.now(), 2, 5, "A1",
                new EstadoProducto(1, "Activo"));
        ProductoResponse prodResp = new ProductoResponse("PROD-0001", new CategoriaResponse(1, "Cuadernos", ""),
                "Cuaderno", new BigDecimal("8000.00"), new BigDecimal("5000.00"), LocalDate.now(), 2, 5, "A1",
                new EstadoProductoResponse(1, "Activo"));
        ProductoPrecioUsd precioUsd = new ProductoPrecioUsd("PROD-0001", "Cuaderno", "Cuadernos",
                new BigDecimal("8000.00"), new BigDecimal("2.00"), new BigDecimal("4000.00"),
                LocalDate.of(2026, 11, 16), TasaCambio.FUENTE_CACHE, 2, "Activo");
        ProductoPrecioUsdResponseDTO precioUsdResp = new ProductoPrecioUsdResponseDTO("PROD-0001", "Cuaderno", "Cuadernos",
                new BigDecimal("8000.00"), new BigDecimal("2.00"), new BigDecimal("4000.00"),
                LocalDate.of(2026, 11, 16), TasaCambio.FUENTE_CACHE, 2, "Activo");

        when(productoUseCase.obtenerTodos()).thenReturn(List.of(prod));
        when(productoUseCase.obtenerPorId("PROD-0001")).thenReturn(prod);
        when(productoUseCase.buscarPorCriterio("Cuaderno")).thenReturn(List.of(prod));
        when(productoUseCase.obtenerProductosStockBajo()).thenReturn(List.of(prod));
        when(productoUseCase.listarPreciosEnUsd()).thenReturn(List.of(precioUsd));
        when(productoUseCase.obtenerPrecioEnUsd("PROD-0001")).thenReturn(precioUsd);
        when(productoRestMapper.toResponse(prod)).thenReturn(prodResp);
        when(productoRestMapper.toPrecioUsdResponse(precioUsd)).thenReturn(precioUsdResp);

        // Act & Assert
        mockMvc.perform(get("/api/catalog/productos")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProducto").value("PROD-0001"));

        mockMvc.perform(get("/api/catalog/productos/PROD-0001")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto").value("PROD-0001"));

        mockMvc.perform(get("/api/catalog/productos/buscar")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("query", "Cuaderno"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProducto").value("PROD-0001"));

        mockMvc.perform(get("/api/catalog/productos/stock-bajo")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value(2));

        mockMvc.perform(get("/api/catalog/productos/precios-usd")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].precioVentaUsd").value(2.00));

        mockMvc.perform(get("/api/catalog/productos/PROD-0001/precio-usd")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fuenteTrm").value("CACHE"));

        mockMvc.perform(get("/api/catalog/productos/PROD-0001/mejor-proveedor")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Permite a Administrador consultar mejor proveedor y realizar escrituras de productos")
    void adminPuedeConsultarMejorProveedorYRealizarOperacionesDeEscritura() throws Exception {
        // Arrange
        String tokenAdmin = "Bearer " + generarToken("admin", "Administrador");
        MejorProveedorProducto mejor = new MejorProveedorProducto(
                "PROD-0001", "Cuaderno", new BigDecimal("10000.00"), 3, "Editorial Norma",
                "9003", "333", "ventas@norma.com", new BigDecimal("5500.00"),
                new BigDecimal("4500.00"), new BigDecimal("45.00"));
        MejorProveedorResponseDTO mejorResp = new MejorProveedorResponseDTO(
                "PROD-0001", "Cuaderno", new BigDecimal("10000.00"), 3, "Editorial Norma",
                "9003", "333", "ventas@norma.com", new BigDecimal("5500.00"),
                new BigDecimal("4500.00"), new BigDecimal("45.00"));

        Producto prod = new Producto("PROD-0001", new Categoria(1, "C", ""), "Cuaderno",
                new BigDecimal("10000.00"), new BigDecimal("5500.00"), LocalDate.now(), 10, 2, "A1",
                new EstadoProducto(1, "Activo"));
        ProductoResponse prodResp = new ProductoResponse("PROD-0001", new CategoriaResponse(1, "C", ""),
                "Cuaderno", new BigDecimal("10000.00"), new BigDecimal("5500.00"), LocalDate.now(), 10, 2, "A1",
                new EstadoProductoResponse(1, "Activo"));

        when(productoUseCase.obtenerMejorProveedor("PROD-0001")).thenReturn(mejor);
        when(productoRestMapper.toMejorProveedorResponse(mejor)).thenReturn(mejorResp);
        when(productoRestMapper.toDomain(any())).thenReturn(prod);
        when(productoUseCase.registrar(any())).thenReturn(prod);
        when(productoUseCase.actualizar(any())).thenReturn(prod);
        when(productoUseCase.descontarStock("PROD-0001", 2)).thenReturn(prod);
        when(productoRestMapper.toResponse(prod)).thenReturn(prodResp);

        String bodyJson = """
                {
                  "idCategoria": 1,
                  "nombre": "Cuaderno",
                  "precioVenta": 10000.00,
                  "costoCompra": 5500.00,
                  "stock": 10,
                  "stockMinimo": 2,
                  "ubicacion": "A1",
                  "idEstado": 1
                }
                """;

        // Act & Assert
        mockMvc.perform(get("/api/catalog/productos/PROD-0001/mejor-proveedor")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreProveedor").value("Editorial Norma"))
                .andExpect(jsonPath("$.porcentajeMargenEstimado").value(45.00));

        mockMvc.perform(post("/api/catalog/productos")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").value("PROD-0001"));

        mockMvc.perform(put("/api/catalog/productos/PROD-0001")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/catalog/productos/PROD-0001/descontar-stock")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .param("cantidad", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto").value("PROD-0001"));

        mockMvc.perform(delete("/api/catalog/productos/PROD-0001")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isNoContent());
    }
}
