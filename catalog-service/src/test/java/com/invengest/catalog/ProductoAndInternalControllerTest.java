package com.invengest.catalog;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.exception.ReglaNegocioException;
import com.invengest.catalog.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.catalog.domain.exception.StockInsuficienteException;
import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.MejorProveedorProducto;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.ProductoPrecioUsd;
import com.invengest.catalog.domain.model.SolicitudItemStock;
import com.invengest.catalog.domain.model.TasaCambio;
import com.invengest.catalog.domain.model.ValidacionItemStock;
import com.invengest.catalog.domain.usecase.ProductoUseCase;
import com.invengest.catalog.infrastructure.entry_points.advice.ErrorResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.catalog.infrastructure.entry_points.dto.response.CategoriaResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.EstadoProductoResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ItemStockValidadoResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.MejorProveedorResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoPrecioUsdResponseDTO;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoResponse;
import com.invengest.catalog.infrastructure.entry_points.rest.InternalProductoController;
import com.invengest.catalog.infrastructure.entry_points.rest.ProductoController;
import com.invengest.catalog.infrastructure.entry_points.security.InternalApiKeyFilter;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.catalog.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.catalog.infrastructure.mapper.ProductoRestMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductoController.class, InternalProductoController.class})
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class ProductoAndInternalControllerTest {

    private static final String JWT_SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String JWT_ISSUER = "invengest-pos-auth";
    private static final String INTERNAL_KEY = "default_dev_internal_api_key_2026";

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
    void endpointsPublicosDeConsultaProductosYPreciosUsdPermitenVendedorPeroMejorProveedorExigeAdmin() throws Exception {
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

        mockMvc.perform(delete("/api/catalog/productos/PROD-0001")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isNoContent());
    }

    @Test
    void endpointsBatchInternosValidaDescuentaYReintegraStockConInternalApiKey() throws Exception {
        // Arrange
        SolicitudItemStock sol = new SolicitudItemStock("PROD-0001", 2);
        ValidacionItemStock val = new ValidacionItemStock("PROD-0001", "Cuaderno", new BigDecimal("8000.00"), 10, 2, true);
        ItemStockValidadoResponseDTO valDto = new ItemStockValidadoResponseDTO("PROD-0001", "Cuaderno", new BigDecimal("8000.00"), 10, 2, true);
        Producto prod = new Producto("PROD-0001", new Categoria(1, "C", ""), "Cuaderno",
                new BigDecimal("8000.00"), new BigDecimal("5000.00"), LocalDate.now(), 8, 2, "A1",
                new EstadoProducto(1, "Activo"));
        ProductoResponse prodResp = new ProductoResponse("PROD-0001", new CategoriaResponse(1, "C", ""),
                "Cuaderno", new BigDecimal("8000.00"), new BigDecimal("5000.00"), LocalDate.now(), 8, 2, "A1",
                new EstadoProductoResponse(1, "Activo"));

        when(productoRestMapper.toSolicitudItemStock(any())).thenReturn(sol);
        when(productoUseCase.validarStockBatch(anyList())).thenReturn(List.of(val));
        when(productoRestMapper.toItemStockValidadoResponse(val)).thenReturn(valDto);
        when(productoUseCase.descontarStockBatch(anyList())).thenReturn(List.of(prod));
        when(productoUseCase.reintegrarStockBatch(anyList())).thenReturn(List.of(prod));
        when(productoRestMapper.toResponse(prod)).thenReturn(prodResp);

        String payloadBatch = "[{\"idProducto\":\"PROD-0001\",\"cantidad\":2}]";

        // Act & Assert
        mockMvc.perform(post("/internal/productos/validar-stock")
                        .header(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, INTERNAL_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadBatch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].suficiente").value(true));

        mockMvc.perform(post("/internal/productos/descontar-stock")
                        .header(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, INTERNAL_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadBatch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value(8));

        mockMvc.perform(post("/internal/productos/reintegrar-stock")
                        .header(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, INTERNAL_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadBatch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProducto").value("PROD-0001"));
    }

    @Test
    void globalExceptionHandlerMapeaTodosLosCodigosHttpCorrectamente() throws Exception {
        // Arrange
        String tokenAdmin = "Bearer " + generarToken("admin", "Administrador");
        GlobalExceptionHandler directHandler = new GlobalExceptionHandler();

        when(productoUseCase.obtenerPorId("NO-EXISTE"))
                .thenThrow(new RecursoNoEncontradoException("No encontrado"));
        when(productoUseCase.descontarStock("PROD-0001", 99))
                .thenThrow(new StockInsuficienteException("Stock insuficiente"));
        when(productoUseCase.listarPreciosEnUsd())
                .thenThrow(new ServicioExternoNoDisponibleException("DolarAPI no disponible"));

        // Act & Assert
        mockMvc.perform(get("/api/catalog/productos/NO-EXISTE")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(put("/api/catalog/productos/PROD-0001/descontar-stock")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .param("cantidad", "99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/api/catalog/productos/precios-usd")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));

        mockMvc.perform(post("/api/catalog/productos")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        ResponseEntity<ErrorResponseDTO> r409 = directHandler.handleConflict(new EntidadDuplicadaException("Duplicado"));
        ResponseEntity<ErrorResponseDTO> r400 = directHandler.handleBusinessRule(new ReglaNegocioException("Regla"));
        ResponseEntity<ErrorResponseDTO> rDb = directHandler.handleDatabaseConstraints(new DataIntegrityViolationException("FK"));
        ResponseEntity<ErrorResponseDTO> r500 = directHandler.handleGenericException(new RuntimeException("Fallo"));

        assertEquals(HttpStatus.CONFLICT, r409.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, r400.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rDb.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r500.getStatusCode());
    }
}
