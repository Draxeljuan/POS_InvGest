package com.invengest.catalog;

import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.ProductoProveedor;
import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.domain.model.Servicio;
import com.invengest.catalog.domain.usecase.CategoriaServicioUseCase;
import com.invengest.catalog.domain.usecase.CategoriaUseCase;
import com.invengest.catalog.domain.usecase.EstadoProductoUseCase;
import com.invengest.catalog.domain.usecase.EstadoProveedorUseCase;
import com.invengest.catalog.domain.usecase.ProductoProveedorUseCase;
import com.invengest.catalog.domain.usecase.ProveedorUseCase;
import com.invengest.catalog.domain.usecase.ServicioUseCase;
import com.invengest.catalog.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.catalog.infrastructure.entry_points.dto.response.CategoriaResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.CategoriaServicioResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.EstadoProductoResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.EstadoProveedorResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProductoProveedorResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ProveedorResponse;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ServicioResponse;
import com.invengest.catalog.infrastructure.entry_points.rest.CategoriaController;
import com.invengest.catalog.infrastructure.entry_points.rest.CategoriaServicioController;
import com.invengest.catalog.infrastructure.entry_points.rest.EstadoProductoController;
import com.invengest.catalog.infrastructure.entry_points.rest.EstadoProveedorController;
import com.invengest.catalog.infrastructure.entry_points.rest.ProductoProveedorController;
import com.invengest.catalog.infrastructure.entry_points.rest.ProveedorController;
import com.invengest.catalog.infrastructure.entry_points.rest.ServicioController;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.catalog.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.catalog.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.catalog.infrastructure.mapper.CategoriaRestMapper;
import com.invengest.catalog.infrastructure.mapper.CategoriaServicioRestMapper;
import com.invengest.catalog.infrastructure.mapper.EstadoProductoRestMapper;
import com.invengest.catalog.infrastructure.mapper.EstadoProveedorRestMapper;
import com.invengest.catalog.infrastructure.mapper.ProductoProveedorRestMapper;
import com.invengest.catalog.infrastructure.mapper.ProveedorRestMapper;
import com.invengest.catalog.infrastructure.mapper.ServicioRestMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

@WebMvcTest(controllers = {
        CategoriaController.class,
        CategoriaServicioController.class,
        EstadoProductoController.class,
        EstadoProveedorController.class,
        ProductoProveedorController.class,
        ProveedorController.class,
        ServicioController.class
})
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class CatalogSecondaryControllersTest {

    private static final String JWT_SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String JWT_ISSUER = "invengest-pos-auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaUseCase categoriaUseCase;
    @MockitoBean
    private CategoriaRestMapper categoriaRestMapper;
    @MockitoBean
    private CategoriaServicioUseCase categoriaServicioUseCase;
    @MockitoBean
    private CategoriaServicioRestMapper categoriaServicioRestMapper;
    @MockitoBean
    private EstadoProductoUseCase estadoProductoUseCase;
    @MockitoBean
    private EstadoProductoRestMapper estadoProductoRestMapper;
    @MockitoBean
    private EstadoProveedorUseCase estadoProveedorUseCase;
    @MockitoBean
    private EstadoProveedorRestMapper estadoProveedorRestMapper;
    @MockitoBean
    private ProductoProveedorUseCase productoProveedorUseCase;
    @MockitoBean
    private ProductoProveedorRestMapper productoProveedorRestMapper;
    @MockitoBean
    private ProveedorUseCase proveedorUseCase;
    @MockitoBean
    private ProveedorRestMapper proveedorRestMapper;
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
    void controladoresSecundariosDeCatalogoResponden200Y201Y204() throws Exception {
        // Arrange
        String tokenAdmin = generarTokenAdmin();
        Categoria cat = new Categoria(1, "Escolar", "Útiles");
        when(categoriaUseCase.obtenerTodas()).thenReturn(List.of(cat));
        when(categoriaUseCase.obtenerPorId(1)).thenReturn(cat);
        when(categoriaUseCase.crear(any())).thenReturn(cat);
        when(categoriaUseCase.actualizar(any())).thenReturn(cat);
        when(categoriaRestMapper.toDomain(any())).thenReturn(cat);
        when(categoriaRestMapper.toResponse(cat)).thenReturn(new CategoriaResponse(1, "Escolar", "Útiles"));

        CategoriaServicio cs = new CategoriaServicio(1, "Impresión");
        when(categoriaServicioUseCase.obtenerTodas()).thenReturn(List.of(cs));
        when(categoriaServicioUseCase.obtenerPorId(1)).thenReturn(cs);
        when(categoriaServicioUseCase.crear(any())).thenReturn(cs);
        when(categoriaServicioUseCase.actualizar(any())).thenReturn(cs);
        when(categoriaServicioRestMapper.toDomain(any())).thenReturn(cs);
        when(categoriaServicioRestMapper.toResponse(cs)).thenReturn(new CategoriaServicioResponse(1, "Impresión"));

        EstadoProducto ep = new EstadoProducto(1, "Activo");
        when(estadoProductoUseCase.obtenerTodos()).thenReturn(List.of(ep));
        when(estadoProductoUseCase.obtenerPorId(1)).thenReturn(ep);
        when(estadoProductoUseCase.obtenerPorNombre("Activo")).thenReturn(ep);
        when(estadoProductoRestMapper.toResponse(ep)).thenReturn(new EstadoProductoResponse(1, "Activo"));

        EstadoProveedor epr = new EstadoProveedor(1, "Activo");
        when(estadoProveedorUseCase.obtenerTodos()).thenReturn(List.of(epr));
        when(estadoProveedorUseCase.obtenerPorId(1)).thenReturn(epr);
        when(estadoProveedorUseCase.obtenerPorNombre("Activo")).thenReturn(epr);
        when(estadoProveedorRestMapper.toResponse(epr)).thenReturn(new EstadoProveedorResponse(1, "Activo"));

        Proveedor prov = new Proveedor(1, epr, "Norma", "300", "a@a.com", "Dir", "900");
        when(proveedorUseCase.obtenerTodos()).thenReturn(List.of(prov));
        when(proveedorUseCase.obtenerPorId(1)).thenReturn(prov);
        when(proveedorUseCase.crear(any())).thenReturn(prov);
        when(proveedorUseCase.actualizar(any())).thenReturn(prov);
        when(proveedorRestMapper.toDomain(any())).thenReturn(prov);
        when(proveedorRestMapper.toResponse(prov))
                .thenReturn(new ProveedorResponse(1, new EstadoProveedorResponse(1, "Activo"), "Norma", "300", "a@a.com", "Dir", "900"));

        ProductoProveedor pp = new ProductoProveedor("PROD-0001", 1, BigDecimal.TEN);
        when(productoProveedorUseCase.obtenerPorProducto("PROD-0001")).thenReturn(List.of(pp));
        when(productoProveedorUseCase.obtenerPorProveedor(1)).thenReturn(List.of(pp));
        when(productoProveedorUseCase.asociarOActualizar(any())).thenReturn(pp);
        when(productoProveedorRestMapper.toDomain(any())).thenReturn(pp);
        when(productoProveedorRestMapper.toResponse(pp)).thenReturn(new ProductoProveedorResponse("PROD-0001", 1, BigDecimal.TEN));

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

        mockMvc.perform(get("/api/catalog/estados-producto").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/estados-producto/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/estados-producto/nombre/Activo").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());

        mockMvc.perform(get("/api/catalog/estados-proveedor").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/estados-proveedor/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/estados-proveedor/nombre/Activo").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());

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

        mockMvc.perform(get("/api/catalog/productos-proveedores/producto/PROD-0001").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/api/catalog/productos-proveedores/proveedor/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isOk());
        mockMvc.perform(post("/api/catalog/productos-proveedores")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProducto\":\"PROD-0001\",\"idProveedor\":1,\"precioCompraProveedor\":4500.00}"))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/catalog/productos-proveedores/producto/PROD-0001/proveedor/1").header(HttpHeaders.AUTHORIZATION, tokenAdmin)).andExpect(status().isNoContent());

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
