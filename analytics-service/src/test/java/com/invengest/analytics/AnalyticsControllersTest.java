package com.invengest.analytics;

import com.invengest.analytics.domain.exception.GeneracionReporteException;
import com.invengest.analytics.domain.exception.RecursoNoEncontradoException;
import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.DashboardResumen;
import com.invengest.analytics.domain.model.ReporteEstacionalidad;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.ResumenVentasUsd;
import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.domain.usecase.ConsultarAlertasStockUseCase;
import com.invengest.analytics.domain.usecase.ConsultarDashboardUseCase;
import com.invengest.analytics.domain.usecase.ConsultarEstacionalidadUseCase;
import com.invengest.analytics.domain.usecase.ConsultarTopProductosUseCase;
import com.invengest.analytics.domain.usecase.GenerarReporteControlVentasUseCase;
import com.invengest.analytics.domain.usecase.SincronizarAnaliticaUseCase;
import com.invengest.analytics.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.analytics.infrastructure.entry_points.rest.AlertaStockController;
import com.invengest.analytics.infrastructure.entry_points.rest.DashboardController;
import com.invengest.analytics.infrastructure.entry_points.rest.EstacionalidadController;
import com.invengest.analytics.infrastructure.entry_points.rest.ReporteController;
import com.invengest.analytics.infrastructure.entry_points.rest.SincronizacionAnaliticaController;
import com.invengest.analytics.infrastructure.entry_points.rest.TopProductoController;
import com.invengest.analytics.infrastructure.entry_points.security.InternalApiKeyFilter;
import com.invengest.analytics.infrastructure.entry_points.security.JsonAccessDeniedHandler;
import com.invengest.analytics.infrastructure.entry_points.security.JsonAuthenticationEntryPoint;
import com.invengest.analytics.infrastructure.entry_points.security.SecurityConfig;
import com.invengest.analytics.infrastructure.mapper.AlertaStockMapperImpl;
import com.invengest.analytics.infrastructure.mapper.AnaliticaRestMapperImpl;
import com.invengest.analytics.infrastructure.mapper.DashboardRestMapperImpl;
import com.invengest.analytics.infrastructure.mapper.ResumenVentaMapperImpl;
import com.invengest.analytics.infrastructure.mapper.TopProductoMapperImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        SincronizacionAnaliticaController.class,
        DashboardController.class,
        TopProductoController.class,
        AlertaStockController.class,
        ReporteController.class,
        EstacionalidadController.class
})
@Import({
        SecurityConfig.class,
        JsonAuthenticationEntryPoint.class,
        JsonAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        ResumenVentaMapperImpl.class,
        TopProductoMapperImpl.class,
        AlertaStockMapperImpl.class,
        DashboardRestMapperImpl.class,
        AnaliticaRestMapperImpl.class
})
@TestPropertySource(properties = {
        "jwt.secret=test_secret_key_for_analytics_service_minimum_32_bytes_2026",
        "jwt.issuer=invengest-pos-auth",
        "internal.api-key=test_internal_key_2026"
})
class AnalyticsControllersTest {

    private static final String JWT_SECRET = "test_secret_key_for_analytics_service_minimum_32_bytes_2026";
    private static final String JWT_ISSUER = "invengest-pos-auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SincronizarAnaliticaUseCase sincronizarAnaliticaUseCase;

    @MockitoBean
    private ConsultarDashboardUseCase consultarDashboardUseCase;

    @MockitoBean
    private ConsultarTopProductosUseCase consultarTopProductosUseCase;

    @MockitoBean
    private ConsultarAlertasStockUseCase consultarAlertasStockUseCase;

    @MockitoBean
    private GenerarReporteControlVentasUseCase generarReporteUseCase;

    @MockitoBean
    private ConsultarEstacionalidadUseCase consultarEstacionalidadUseCase;

    private String generarTokenBearer(String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(username)
                .issuer(JWT_ISSUER)
                .claim("role", role)
                .claim("userId", 1)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
        return "Bearer " + token;
    }

    @Test
    @DisplayName("POST /internal/ventas, /internal/ventas/anulacion y /internal/alertas-stock deben exigir X-Internal-Api-Key")
    void endpointsInternos_SincronizacionConApiKey() throws Exception {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        ResumenVentasDiarias resumen = ResumenVentasDiarias.builder()
                .fecha(fecha)
                .totalVentas(new BigDecimal("100000.00"))
                .totalAnulado(new BigDecimal("20000.00"))
                .cantidadOperaciones(4)
                .build();
        AlertaStock alerta = new AlertaStock(
                1, "PROD-01", "Cuaderno", (short) 2, (short) 5, "Bajo Stock", LocalDateTime.now()
        );

        when(sincronizarAnaliticaUseCase.registrarVentaSincronizada(eq(fecha), eq(new BigDecimal("100000.00")), any()))
                .thenReturn(resumen);
        when(sincronizarAnaliticaUseCase.registrarAnulacionSincronizada(eq(fecha), eq(new BigDecimal("20000.00")), any()))
                .thenReturn(resumen);
        when(sincronizarAnaliticaUseCase.sincronizarAlertaStock("PROD-01", "Cuaderno", (short) 2, (short) 5))
                .thenReturn(Optional.of(alerta));
        when(sincronizarAnaliticaUseCase.sincronizarAlertaStock("PROD-01", "Cuaderno", (short) 15, (short) 5))
                .thenReturn(Optional.empty());

        // Act & Assert (Venta sincronizada)
        mockMvc.perform(post("/internal/ventas")
                        .header(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, "test_internal_key_2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha\":\"2026-11-16\",\"totalVenta\":100000.00,\"items\":[{\"idProducto\":\"PROD-01\",\"nombreProducto\":\"Cuaderno\",\"cantidadVendida\":2}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventasNetas").value(80000.00));

        // Act & Assert (Anulación sincronizada)
        mockMvc.perform(post("/internal/ventas/anulacion")
                        .header(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, "test_internal_key_2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idVenta\":10,\"fechaVenta\":\"2026-11-16\",\"totalAnulado\":20000.00,\"items\":[{\"idProducto\":\"PROD-01\",\"cantidadAnulada\":1}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnulado").value(20000.00));

        // Act & Assert (Alerta stock activa -> 200 y resuelta -> 204)
        mockMvc.perform(post("/internal/alertas-stock")
                        .header(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, "test_internal_key_2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProducto\":\"PROD-01\",\"nombreProducto\":\"Cuaderno\",\"stockActual\":2,\"stockMinimo\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockMinimo").value(5));

        mockMvc.perform(post("/internal/alertas-stock")
                        .header(InternalApiKeyFilter.HEADER_INTERNAL_API_KEY, "test_internal_key_2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProducto\":\"PROD-01\",\"nombreProducto\":\"Cuaderno\",\"stockActual\":15,\"stockMinimo\":5}"))
                .andExpect(status().isNoContent());

        // Act & Assert (Sin llave interna -> 401)
        mockMvc.perform(post("/internal/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"totalVenta\":50000.00}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Endpoints de consulta operativa (Dashboard, Top Productos, Alertas, Estacionalidad) deben permitir Vendedor y bloquear Reportes (403)")
    void endpointsConsultaYRBAC_Vendedor() throws Exception {
        // Arrange
        String tokenVendedor = generarTokenBearer("vendedor", "Vendedor");
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        DashboardResumen dashboard = DashboardResumen.builder()
                .fechaConsulta(fecha)
                .ventasDelDia(new BigDecimal("100000.00"))
                .ventasAnuladasDelDia(BigDecimal.ZERO)
                .ventasNetasDelDia(new BigDecimal("100000.00"))
                .operacionesDelDia(3)
                .ventasAcumuladasPeriodo(new BigDecimal("250000.00"))
                .historialVentasRecientes(List.of())
                .productosMasVendidos(List.of())
                .alertasRecientes(List.of())
                .build();
        ReporteEstacionalidad estacionalidad = ReporteEstacionalidad.builder()
                .anio(2026)
                .mes(11)
                .diasHabiles(24)
                .diasNoHabiles(6)
                .promedioVentasDiaHabil(new BigDecimal("120000.00"))
                .promedioVentasDiaNoHabil(new BigDecimal("80000.00"))
                .fuenteFestivos("CACHE")
                .build();

        when(consultarDashboardUseCase.obtenerResumenDashboard(fecha, 7)).thenReturn(dashboard);
        when(consultarDashboardUseCase.consultarVentasPorRango(fecha, fecha)).thenReturn(List.of());
        when(consultarTopProductosUseCase.obtenerPorFecha(fecha)).thenReturn(List.of(new TopProducto(1, "P1", "Prod", 5, fecha)));
        when(consultarTopProductosUseCase.obtenerTopPorRango(fecha, fecha, 10)).thenReturn(List.of());
        when(consultarAlertasStockUseCase.obtenerTodas()).thenReturn(List.of());
        when(consultarAlertasStockUseCase.obtenerPorEstado("Bajo Stock")).thenReturn(List.of());
        when(consultarAlertasStockUseCase.obtenerRecientes(5)).thenReturn(List.of());
        when(consultarEstacionalidadUseCase.analizarEstacionalidadMensual(2026, 11)).thenReturn(estacionalidad);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("fecha", "2026-11-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventasNetasDelDia").value(100000.00));

        mockMvc.perform(get("/api/analytics/dashboard/ventas")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("inicio", "2026-11-16")
                        .param("fin", "2026-11-16"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/analytics/top-products")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("fecha", "2026-11-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idProducto").value("P1"));

        mockMvc.perform(get("/api/analytics/top-products/rango")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("inicio", "2026-11-16")
                        .param("fin", "2026-11-16"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/analytics/alerts")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/analytics/alerts")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("estado", "Bajo Stock"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/analytics/alerts/recientes")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("limite", "5"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/analytics/estacionalidad")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("anio", "2026")
                        .param("mes", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasHabiles").value(24));

        // Vendedor intentando descargar reportes gerenciales -> 403 JSON
        mockMvc.perform(get("/api/analytics/reports/ventas/excel")
                        .header(HttpHeaders.AUTHORIZATION, tokenVendedor)
                        .param("anio", "2026")
                        .param("mes", "11"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Administrador debe descargar reportes Excel/PDF, resumen USD y recibir códigos HTTP estándar ante errores")
    void endpointsReportesYGlobalExceptionHandler_Administrador() throws Exception {
        // Arrange
        String tokenAdmin = generarTokenBearer("admin", "Administrador");
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);
        byte[] dummyExcel = new byte[]{80, 75, 3, 4};
        byte[] dummyPdf = new byte[]{37, 80, 68, 70};
        ResumenVentasUsd resumenUsd = ResumenVentasUsd.builder()
                .fechaInicio(inicio)
                .fechaFin(fin)
                .totalVendidoCop(new BigDecimal("400000.00"))
                .totalAnuladoCop(new BigDecimal("50000.00"))
                .totalNetoCop(new BigDecimal("350000.00"))
                .totalVendidoUsd(new BigDecimal("114.29"))
                .totalAnuladoUsd(new BigDecimal("14.29"))
                .totalNetoUsd(new BigDecimal("100.00"))
                .cantidadOperaciones(10)
                .tasaCambioTrm(new BigDecimal("3500.00"))
                .fechaVigenciaTrm(LocalDate.of(2026, 11, 16))
                .fuenteTrm("CACHE")
                .build();

        when(generarReporteUseCase.generarExcelControlVentas(2026, 11)).thenReturn(dummyExcel);
        when(generarReporteUseCase.generarPdfVentasPorRango(inicio, fin)).thenReturn(dummyPdf);
        when(generarReporteUseCase.generarPdfAlertasStock()).thenReturn(dummyPdf);
        when(generarReporteUseCase.obtenerResumenVentasUsd(inicio, fin)).thenReturn(resumenUsd);

        // Act & Assert (Casos exitosos)
        mockMvc.perform(get("/api/analytics/reports/ventas/excel")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .param("anio", "2026")
                        .param("mes", "11"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"control_ventas_2026_11.xlsx\""))
                .andExpect(content().bytes(dummyExcel));

        mockMvc.perform(get("/api/analytics/reports/ventas/pdf")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .param("inicio", "2026-11-01")
                        .param("fin", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        mockMvc.perform(get("/api/analytics/reports/alertas-stock/pdf")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        mockMvc.perform(get("/api/analytics/reports/ventas/resumen-usd")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .param("inicio", "2026-11-01")
                        .param("fin", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNetoUsd").value(100.00))
                .andExpect(jsonPath("$.fuenteTrm").value("CACHE"));

        // Arrange & Act & Assert (Errores manejados por GlobalExceptionHandler: 400, 404, 500, 503)
        when(generarReporteUseCase.generarExcelControlVentas(2026, 15))
                .thenThrow(new ReglaNegocioException("El mes debe estar comprendido entre 1 y 12."));
        mockMvc.perform(get("/api/analytics/reports/ventas/excel")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .param("anio", "2026")
                        .param("mes", "15"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        when(generarReporteUseCase.obtenerResumenVentasUsd(inicio, inicio))
                .thenThrow(new ServicioExternoNoDisponibleException("TRM no disponible"));
        mockMvc.perform(get("/api/analytics/reports/ventas/resumen-usd")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .param("inicio", "2026-11-01")
                        .param("fin", "2026-11-01"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));

        when(generarReporteUseCase.generarPdfAlertasStock())
                .thenThrow(new GeneracionReporteException("Fallo PDF", new RuntimeException("IO")));
        mockMvc.perform(get("/api/analytics/reports/alertas-stock/pdf")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));

        when(consultarDashboardUseCase.obtenerResumenDashboard(LocalDate.of(2025, 1, 1), 7))
                .thenThrow(new RecursoNoEncontradoException("No encontrado"));
        mockMvc.perform(get("/api/analytics/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .param("fecha", "2025-01-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // Direct test of remaining GlobalExceptionHandler branches
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        assertThat(handler.handleDatabaseConstraints(new DataIntegrityViolationException("Constraint")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(handler.handleGenericException(new RuntimeException("Fallo")).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
