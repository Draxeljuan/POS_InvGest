package com.invengest.transaction;

import com.invengest.transaction.domain.exception.EntidadDuplicadaException;
import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.transaction.domain.exception.StockInsuficienteException;
import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.ComprobanteVenta;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.model.ResumenCaja;
import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.usecase.AnularVentaUseCase;
import com.invengest.transaction.domain.usecase.CajaUseCase;
import com.invengest.transaction.domain.usecase.ClienteUseCase;
import com.invengest.transaction.domain.usecase.ComprobanteVentaUseCase;
import com.invengest.transaction.domain.usecase.ConsultarVentaUseCase;
import com.invengest.transaction.domain.usecase.MetodoPagoUseCase;
import com.invengest.transaction.domain.usecase.MovimientoInventarioUseCase;
import com.invengest.transaction.domain.usecase.RegistrarVentaUseCase;
import com.invengest.transaction.domain.usecase.TipoDocumentoUseCase;
import com.invengest.transaction.domain.usecase.TipoMovimientoUseCase;
import com.invengest.transaction.infrastructure.entry_points.advice.ErrorResponseDTO;
import com.invengest.transaction.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.transaction.infrastructure.entry_points.dto.response.CierreCajaResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ClienteResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ComprobanteVentaResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.MetodoPagoResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.MovimientoInventarioResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ResumenCajaResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.TipoDocumentoResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.TipoMovimientoResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.VentaResponse;
import com.invengest.transaction.infrastructure.entry_points.rest.CajaController;
import com.invengest.transaction.infrastructure.entry_points.rest.ClienteController;
import com.invengest.transaction.infrastructure.entry_points.rest.MetodoPagoController;
import com.invengest.transaction.infrastructure.entry_points.rest.MovimientoInventarioController;
import com.invengest.transaction.infrastructure.entry_points.rest.TipoDocumentoController;
import com.invengest.transaction.infrastructure.entry_points.rest.TipoMovimientoController;
import com.invengest.transaction.infrastructure.entry_points.rest.VentaController;
import com.invengest.transaction.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.transaction.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.transaction.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.transaction.infrastructure.mapper.CajaRestMapper;
import com.invengest.transaction.infrastructure.mapper.ClienteRestMapper;
import com.invengest.transaction.infrastructure.mapper.MetodoPagoRestMapper;
import com.invengest.transaction.infrastructure.mapper.MovimientoInventarioRestMapper;
import com.invengest.transaction.infrastructure.mapper.TipoDocumentoRestMapper;
import com.invengest.transaction.infrastructure.mapper.TipoMovimientoRestMapper;
import com.invengest.transaction.infrastructure.mapper.VentaRestMapper;
import feign.FeignException;
import feign.Request;
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
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        VentaController.class,
        CajaController.class,
        MovimientoInventarioController.class,
        ClienteController.class,
        MetodoPagoController.class,
        TipoDocumentoController.class,
        TipoMovimientoController.class
})
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class TransactionControllersTest {

    private static final String JWT_SECRET = "default_dev_secret_key_must_be_at_least_32_bytes_long_2026";
    private static final String JWT_ISSUER = "invengest-pos-auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarVentaUseCase registrarVentaUseCase;
    @MockitoBean
    private AnularVentaUseCase anularVentaUseCase;
    @MockitoBean
    private ConsultarVentaUseCase consultarVentaUseCase;
    @MockitoBean
    private ComprobanteVentaUseCase comprobanteVentaUseCase;
    @MockitoBean
    private CajaUseCase cajaUseCase;
    @MockitoBean
    private MovimientoInventarioUseCase movimientoInventarioUseCase;
    @MockitoBean
    private ClienteUseCase clienteUseCase;
    @MockitoBean
    private MetodoPagoUseCase metodoPagoUseCase;
    @MockitoBean
    private TipoDocumentoUseCase tipoDocumentoUseCase;
    @MockitoBean
    private TipoMovimientoUseCase tipoMovimientoUseCase;

    @MockitoBean
    private VentaRestMapper ventaRestMapper;
    @MockitoBean
    private CajaRestMapper cajaRestMapper;
    @MockitoBean
    private MovimientoInventarioRestMapper movimientoInventarioRestMapper;
    @MockitoBean
    private ClienteRestMapper clienteRestMapper;
    @MockitoBean
    private MetodoPagoRestMapper metodoPagoRestMapper;
    @MockitoBean
    private TipoDocumentoRestMapper tipoDocumentoRestMapper;
    @MockitoBean
    private TipoMovimientoRestMapper tipoMovimientoRestMapper;

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
    void ventaControllerRegistraConsultaComprobanteYRestringeAnulacionAAdministrador() throws Exception {
        // Arrange
        String tokenVendedor = "Bearer " + generarToken("vendedor", "Vendedor");
        String tokenAdmin = "Bearer " + generarToken("admin", "Administrador");

        Venta venta = new Venta();
        venta.setIdVenta(10);
        VentaResponse ventaResp = new VentaResponse(
                10, 1, LocalDateTime.now(), null, new MetodoPagoResponse(1, "Efectivo"),
                new BigDecimal("15000.00"), "COMPLETADA", null, null, null, List.of()
        );
        ComprobanteVenta comp = new ComprobanteVenta(
                10, "POS-000010", LocalDateTime.now(), "COMPLETADA", 1,
                "Consumidor Final", null, "cliente@correo.com", "Efectivo", new BigDecimal("15000.00"), List.of()
        );
        ComprobanteVentaResponse compResp = new ComprobanteVentaResponse(
                10, "POS-000010", LocalDateTime.now(), "COMPLETADA", 1,
                "Consumidor Final", null, "cliente@correo.com", "Efectivo", new BigDecimal("15000.00"), List.of()
        );

        when(ventaRestMapper.toDomain(any())).thenReturn(venta);
        when(registrarVentaUseCase.registrarVenta(any())).thenReturn(venta);
        when(anularVentaUseCase.anularVenta(10, 1, "Devolución")).thenReturn(venta);
        when(consultarVentaUseCase.obtenerPorId(10)).thenReturn(venta);
        when(consultarVentaUseCase.listarTodas()).thenReturn(List.of(venta));
        when(consultarVentaUseCase.listarPorUsuario(1)).thenReturn(List.of(venta));
        when(consultarVentaUseCase.filtrarPorFechas(any(), any())).thenReturn(List.of(venta));
        when(comprobanteVentaUseCase.obtenerComprobante(10)).thenReturn(comp);
        when(comprobanteVentaUseCase.enviarComprobantePorCorreo(eq(10), any())).thenReturn(comp);
        when(ventaRestMapper.toResponse(venta)).thenReturn(ventaResp);
        when(ventaRestMapper.toComprobanteResponse(comp)).thenReturn(compResp);

        String ventaBody = """
                {
                  "idUsuario": 1,
                  "idMetodoPago": 1,
                  "items": [
                    {
                      "idItem": "PROD-01",
                      "tipoItem": "PRODUCTO",
                      "cantidad": 2
                    }
                  ]
                }
                """;
        String anularBody = """
                {
                  "idUsuarioAnulacion": 1,
                  "motivoAnulacion": "Devolución"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/transactions/ventas")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ventaBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idVenta").value(10));

        // Vendedor NO puede anular venta (403)
        mockMvc.perform(post("/api/transactions/ventas/10/anular")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(anularBody))
                .andExpect(status().isForbidden());

        // Administrador SÍ puede anular venta (200)
        mockMvc.perform(post("/api/transactions/ventas/10/anular")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(anularBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idVenta").value(10));

        mockMvc.perform(get("/api/transactions/ventas/10/comprobante")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroComprobante").value("POS-000010"));

        mockMvc.perform(post("/api/transactions/ventas/10/comprobante/enviar")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailDestino\":\"cliente@correo.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailCliente").value("cliente@correo.com"));

        mockMvc.perform(get("/api/transactions/ventas/10")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transactions/ventas")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transactions/ventas")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("idUsuario", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transactions/ventas")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("inicio", "2026-11-01T00:00:00")
                        .param("fin", "2026-11-16T23:59:59"))
                .andExpect(status().isOk());
    }

    @Test
    void cajaKardexYCatalogosAuxiliaresFuncionanSegunRoles() throws Exception {
        // Arrange
        String tokenVendedor = "Bearer " + generarToken("vendedor", "Vendedor");
        String tokenAdmin = "Bearer " + generarToken("admin", "Administrador");

        ResumenCaja resumen = new ResumenCaja(LocalDate.of(2026, 11, 16),
                new BigDecimal("50000.00"), 3, BigDecimal.ZERO, 0, new BigDecimal("30000.00"), List.of(), false);
        ResumenCajaResponse resumenResp = new ResumenCajaResponse(LocalDate.of(2026, 11, 16),
                new BigDecimal("50000.00"), 3, BigDecimal.ZERO, 0, new BigDecimal("30000.00"), List.of(), false);

        CierreCaja cierre = new CierreCaja();
        cierre.setIdCierre(1);
        CierreCajaResponse cierreResp = new CierreCajaResponse(
                1, LocalDate.of(2026, 11, 16), 1, new BigDecimal("50000.00"), 3,
                new BigDecimal("30000.00"), new BigDecimal("30000.00"), BigDecimal.ZERO,
                false, LocalDate.of(2026, 11, 17), "OK", LocalDateTime.now(), List.of()
        );

        MovimientoInventario mov = new MovimientoInventario();
        MovimientoInventarioResponse movResp = new MovimientoInventarioResponse(
                1, "PROD-01", 1, new TipoMovimientoResponse(3, "Ajuste por Pérdida/Daño"),
                2, LocalDateTime.now(), "Rotura"
        );

        Cliente cli = new Cliente();
        ClienteResponse cliResp = new ClienteResponse(1, new TipoDocumentoResponse(1, "CC"),
                "100", "Juan", null, "Pérez", null, "Juan Pérez", "300", "j@test.com");

        when(cajaUseCase.obtenerResumenCaja(any())).thenReturn(resumen);
        when(cajaRestMapper.toResumenResponse(resumen)).thenReturn(resumenResp);
        when(cajaUseCase.registrarCierreCaja(any(), eq(1), any(), any())).thenReturn(cierre);
        when(cajaUseCase.obtenerCierrePorFecha(any())).thenReturn(cierre);
        when(cajaUseCase.listarCierres()).thenReturn(List.of(cierre));
        when(cajaRestMapper.toCierreResponse(cierre)).thenReturn(cierreResp);

        when(movimientoInventarioRestMapper.toDomain(any())).thenReturn(mov);
        when(movimientoInventarioUseCase.registrarAjuste(any())).thenReturn(mov);
        when(movimientoInventarioUseCase.registrarMovimiento(any())).thenReturn(mov);
        when(movimientoInventarioUseCase.obtenerKardexPorProducto("PROD-01")).thenReturn(List.of(mov));
        when(movimientoInventarioUseCase.listarTodos()).thenReturn(List.of(mov));
        when(movimientoInventarioUseCase.filtrarPorFechas(any(), any())).thenReturn(List.of(mov));
        when(movimientoInventarioRestMapper.toResponse(mov)).thenReturn(movResp);

        when(clienteRestMapper.toDomain(any())).thenReturn(cli);
        when(clienteUseCase.registrar(any())).thenReturn(cli);
        when(clienteUseCase.obtenerPorId(1)).thenReturn(cli);
        when(clienteUseCase.obtenerPorDocumento("100")).thenReturn(cli);
        when(clienteUseCase.listarTodos()).thenReturn(List.of(cli));
        when(clienteRestMapper.toResponse(cli)).thenReturn(cliResp);

        MetodoPago mp = new MetodoPago(1, "Efectivo");
        when(metodoPagoUseCase.listarTodos()).thenReturn(List.of(mp));
        when(metodoPagoUseCase.obtenerPorId(1)).thenReturn(mp);
        when(metodoPagoRestMapper.toResponse(mp)).thenReturn(new MetodoPagoResponse(1, "Efectivo"));

        TipoDocumento td = new TipoDocumento(1, "CC");
        when(tipoDocumentoUseCase.listarTodos()).thenReturn(List.of(td));
        when(tipoDocumentoUseCase.obtenerPorId(1)).thenReturn(td);
        when(tipoDocumentoRestMapper.toResponse(td)).thenReturn(new TipoDocumentoResponse(1, "CC"));

        TipoMovimiento tm = new TipoMovimiento(1, "Ingreso");
        when(tipoMovimientoUseCase.listarTodos()).thenReturn(List.of(tm));
        when(tipoMovimientoUseCase.obtenerPorId(1)).thenReturn(tm);
        when(tipoMovimientoRestMapper.toResponse(tm)).thenReturn(new TipoMovimientoResponse(1, "Ingreso"));

        String cierreBody = """
                {
                  "fechaCierre": "2026-11-16",
                  "idUsuario": 1,
                  "efectivoContado": 30000.00,
                  "observacion": "OK"
                }
                """;
        String ajusteBody = """
                {
                  "idProducto": "PROD-01",
                  "idUsuario": 1,
                  "idTipoMovimiento": 3,
                  "cantidad": 2,
                  "observacion": "Rotura"
                }
                """;

        // Act & Assert
        mockMvc.perform(get("/api/transactions/caja/resumen")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("fecha", "2026-11-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.efectivoEsperado").value(30000.00));

        mockMvc.perform(post("/api/transactions/caja/cierre")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cierreBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.esDiaHabil").value(false));

        mockMvc.perform(get("/api/transactions/caja/cierres/2026-11-16")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transactions/caja/cierres")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        // Ajuste manual Kardex: prohibido para Vendedor (403), permitido para Administrador (201)
        mockMvc.perform(post("/api/transactions/kardex/ajustes")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ajusteBody))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/transactions/kardex/ajustes")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ajusteBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/transactions/kardex/movimientos")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ajusteBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/transactions/kardex/productos/PROD-01")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transactions/kardex/movimientos")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transactions/kardex/movimientos")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("inicio", "2026-11-01T00:00:00")
                        .param("fin", "2026-11-16T23:59:59"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/transactions/clientes")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"primerNombre\":\"Juan\",\"primerApellido\":\"Pérez\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/transactions/clientes/1")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transactions/clientes/documento/100")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transactions/clientes")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transactions/metodos-pago")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/transactions/metodos-pago/1")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transactions/tipos-documento")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/transactions/tipos-documento/1")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transactions/tipos-movimiento")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/transactions/tipos-movimiento/1")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());
    }

    @Test
    void globalExceptionHandlerTraduceTodasLasExcepcionesAJsonEstandar() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Request req = Request.create(Request.HttpMethod.GET, "/test", Map.of(), null, StandardCharsets.UTF_8, null);

        // Act
        ResponseEntity<ErrorResponseDTO> r404 = handler.handleNotFound(new RecursoNoEncontradoException("No existe"));
        ResponseEntity<ErrorResponseDTO> r409 = handler.handleConflict(new EntidadDuplicadaException("Duplicado"));
        ResponseEntity<ErrorResponseDTO> rStock = handler.handleInsufficientStock(new StockInsuficienteException("Sin stock"));
        ResponseEntity<ErrorResponseDTO> r400 = handler.handleBusinessRule(new ReglaNegocioException("Regla"));
        ResponseEntity<ErrorResponseDTO> r503 = handler.handleExternalServiceUnavailable(new ServicioExternoNoDisponibleException("API caída"));
        ResponseEntity<ErrorResponseDTO> rFeign404 = handler.handleFeignException(new FeignException.NotFound("404", req, null, null));
        ResponseEntity<ErrorResponseDTO> rFeign503 = handler.handleFeignException(new FeignException.ServiceUnavailable("503", req, null, null));
        ResponseEntity<ErrorResponseDTO> rDb = handler.handleDatabaseConstraints(new DataIntegrityViolationException("FK"));
        ResponseEntity<ErrorResponseDTO> r500 = handler.handleGenericException(new RuntimeException("Error"));

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, r404.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, r409.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rStock.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, r400.getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, r503.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, rFeign404.getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, rFeign503.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rDb.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r500.getStatusCode());
    }
}
