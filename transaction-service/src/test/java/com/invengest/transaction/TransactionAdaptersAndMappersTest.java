package com.invengest.transaction;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.transaction.domain.exception.StockInsuficienteException;
import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.CierreCajaDetalle;
import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.DetalleVenta;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.model.SolicitudStockItem;
import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.domain.model.TipoItem;
import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.CierreCajaDataGatewayImpl;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.ClienteDataGatewayImpl;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.MetodoPagoDataGatewayImpl;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.MovimientoInventarioDataGatewayImpl;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.TipoDocumentoDataGatewayImpl;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.TipoMovimientoDataGatewayImpl;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.VentaDataGatewayImpl;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.CierreCajaData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.CierreCajaDetalleData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.ClienteData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.DetalleVentaData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.MetodoPagoData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.MovimientoInventarioData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.TipoDocumentoData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.TipoMovimientoData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.VentaData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.CierreCajaJpaRepository;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.ClienteJpaRepository;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.MetodoPagoJpaRepository;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.MovimientoInventarioJpaRepository;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.TipoDocumentoJpaRepository;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.TipoMovimientoJpaRepository;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.VentaJpaRepository;
import com.invengest.transaction.infrastructure.driver_adapters.feign.AnalyticsFeignAdapter;
import com.invengest.transaction.infrastructure.driver_adapters.feign.AnalyticsFeignClient;
import com.invengest.transaction.infrastructure.driver_adapters.feign.AuthFeignClient;
import com.invengest.transaction.infrastructure.driver_adapters.feign.CatalogFeignAdapter;
import com.invengest.transaction.infrastructure.driver_adapters.feign.CatalogFeignClient;
import com.invengest.transaction.infrastructure.driver_adapters.feign.InternalFeignRequestInterceptor;
import com.invengest.transaction.infrastructure.driver_adapters.feign.NotificacionFeignAdapter;
import com.invengest.transaction.infrastructure.driver_adapters.feign.NotificationFeignClient;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.CatalogProductoFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.CatalogServicioFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ContactoUsuarioFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ItemStockValidadoFeignDTO;
import com.invengest.transaction.infrastructure.mapper.CierreCajaMapper;
import com.invengest.transaction.infrastructure.mapper.ClienteMapper;
import com.invengest.transaction.infrastructure.mapper.ClienteRestMapper;
import com.invengest.transaction.infrastructure.mapper.MetodoPagoMapper;
import com.invengest.transaction.infrastructure.mapper.MovimientoInventarioMapper;
import com.invengest.transaction.infrastructure.mapper.MovimientoInventarioRestMapper;
import com.invengest.transaction.infrastructure.mapper.TipoDocumentoMapper;
import com.invengest.transaction.infrastructure.mapper.TipoMovimientoMapper;
import com.invengest.transaction.infrastructure.mapper.VentaMapper;
import com.invengest.transaction.infrastructure.mapper.VentaRestMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionAdaptersAndMappersTest {

    @Test
    void dataGatewayImplsPersistenYConsultanCorrectamente() {
        // Arrange
        VentaJpaRepository ventaRepo = mock(VentaJpaRepository.class);
        VentaMapper ventaMapper = mock(VentaMapper.class);
        VentaDataGatewayImpl ventaGw = new VentaDataGatewayImpl(ventaRepo, ventaMapper);

        Venta ventaDom = new Venta();
        ventaDom.setIdVenta(1);
        VentaData ventaEnt = new VentaData();
        ventaEnt.setIdVenta(1);
        ventaEnt.setDetalles(List.of(new DetalleVentaData()));

        when(ventaMapper.toEntity(ventaDom)).thenReturn(ventaEnt);
        when(ventaRepo.save(ventaEnt)).thenReturn(ventaEnt);
        when(ventaMapper.toDomain(ventaEnt)).thenReturn(ventaDom);
        when(ventaRepo.findByIdWithDetails(1)).thenReturn(Optional.empty());
        when(ventaRepo.findById(1)).thenReturn(Optional.of(ventaEnt));
        when(ventaRepo.findAll()).thenReturn(List.of(ventaEnt));
        when(ventaRepo.findByFechaBetween(any(), any())).thenReturn(List.of(ventaEnt));
        when(ventaRepo.findByIdUsuario(1)).thenReturn(List.of(ventaEnt));

        MovimientoInventarioJpaRepository movRepo = mock(MovimientoInventarioJpaRepository.class);
        MovimientoInventarioMapper movMapper = mock(MovimientoInventarioMapper.class);
        MovimientoInventarioDataGatewayImpl movGw = new MovimientoInventarioDataGatewayImpl(movRepo, movMapper);
        MovimientoInventario movDom = new MovimientoInventario();
        MovimientoInventarioData movEnt = new MovimientoInventarioData();
        when(movMapper.toEntity(movDom)).thenReturn(movEnt);
        when(movRepo.save(movEnt)).thenReturn(movEnt);
        when(movMapper.toDomain(movEnt)).thenReturn(movDom);
        when(movRepo.findById(1)).thenReturn(Optional.of(movEnt));
        when(movRepo.findAll()).thenReturn(List.of(movEnt));
        when(movRepo.findByIdProductoOrderByFechaMovimientoDesc("P1")).thenReturn(List.of(movEnt));
        when(movRepo.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(any(), any())).thenReturn(List.of(movEnt));

        CierreCajaJpaRepository cierreRepo = mock(CierreCajaJpaRepository.class);
        CierreCajaMapper cierreMapper = mock(CierreCajaMapper.class);
        CierreCajaDataGatewayImpl cierreGw = new CierreCajaDataGatewayImpl(cierreRepo, cierreMapper);
        CierreCaja cierreDom = new CierreCaja();
        CierreCajaData cierreEnt = new CierreCajaData();
        cierreEnt.setDetalles(List.of(new CierreCajaDetalleData()));
        when(cierreMapper.toEntity(cierreDom)).thenReturn(cierreEnt);
        when(cierreRepo.save(cierreEnt)).thenReturn(cierreEnt);
        when(cierreMapper.toDomain(cierreEnt)).thenReturn(cierreDom);
        when(cierreRepo.findById(1)).thenReturn(Optional.of(cierreEnt));
        when(cierreRepo.findByFechaCierre(any())).thenReturn(Optional.of(cierreEnt));
        when(cierreRepo.existsByFechaCierre(any())).thenReturn(true);
        when(cierreRepo.findAll()).thenReturn(List.of(cierreEnt));

        ClienteJpaRepository cliRepo = mock(ClienteJpaRepository.class);
        ClienteMapper cliMapper = mock(ClienteMapper.class);
        ClienteDataGatewayImpl cliGw = new ClienteDataGatewayImpl(cliRepo, cliMapper);
        Cliente cliDom = new Cliente();
        ClienteData cliEnt = new ClienteData();
        when(cliMapper.toEntity(cliDom)).thenReturn(cliEnt);
        when(cliRepo.save(cliEnt)).thenReturn(cliEnt);
        when(cliMapper.toDomain(cliEnt)).thenReturn(cliDom);
        when(cliRepo.findById(1)).thenReturn(Optional.of(cliEnt));
        when(cliRepo.findByDocumentoIdentidad("123")).thenReturn(Optional.of(cliEnt));
        when(cliRepo.findAll()).thenReturn(List.of(cliEnt));
        when(cliRepo.existsById(1)).thenReturn(true);

        MetodoPagoJpaRepository mpRepo = mock(MetodoPagoJpaRepository.class);
        MetodoPagoMapper mpMapper = mock(MetodoPagoMapper.class);
        MetodoPagoDataGatewayImpl mpGw = new MetodoPagoDataGatewayImpl(mpRepo, mpMapper);
        MetodoPago mpDom = new MetodoPago(1, "Efectivo");
        MetodoPagoData mpEnt = new MetodoPagoData(1, "Efectivo");
        when(mpRepo.findById(1)).thenReturn(Optional.of(mpEnt));
        when(mpRepo.findAll()).thenReturn(List.of(mpEnt));
        when(mpRepo.existsById(1)).thenReturn(true);
        when(mpMapper.toDomain(mpEnt)).thenReturn(mpDom);

        TipoMovimientoJpaRepository tmRepo = mock(TipoMovimientoJpaRepository.class);
        TipoMovimientoMapper tmMapper = mock(TipoMovimientoMapper.class);
        TipoMovimientoDataGatewayImpl tmGw = new TipoMovimientoDataGatewayImpl(tmRepo, tmMapper);
        TipoMovimiento tmDom = new TipoMovimiento(1, "Ingreso");
        TipoMovimientoData tmEnt = new TipoMovimientoData(1, "Ingreso");
        when(tmRepo.findById(1)).thenReturn(Optional.of(tmEnt));
        when(tmRepo.findByNombre("Ingreso")).thenReturn(Optional.of(tmEnt));
        when(tmRepo.findAll()).thenReturn(List.of(tmEnt));
        when(tmMapper.toDomain(tmEnt)).thenReturn(tmDom);

        TipoDocumentoJpaRepository tdRepo = mock(TipoDocumentoJpaRepository.class);
        TipoDocumentoMapper tdMapper = mock(TipoDocumentoMapper.class);
        TipoDocumentoDataGatewayImpl tdGw = new TipoDocumentoDataGatewayImpl(tdRepo, tdMapper);
        TipoDocumento tdDom = new TipoDocumento(1, "CC");
        TipoDocumentoData tdEnt = new TipoDocumentoData(1, "CC");
        when(tdRepo.findById(1)).thenReturn(Optional.of(tdEnt));
        when(tdRepo.findAll()).thenReturn(List.of(tdEnt));
        when(tdMapper.toDomain(tdEnt)).thenReturn(tdDom);

        // Act & Assert
        assertEquals(1, ventaGw.save(ventaDom).getIdVenta());
        assertTrue(ventaGw.findById(1).isPresent());
        assertEquals(1, ventaGw.findAll().size());
        assertEquals(1, ventaGw.findByFechaBetween(LocalDateTime.now(), LocalDateTime.now()).size());
        assertEquals(1, ventaGw.findByIdUsuario(1).size());

        assertEquals(movDom, movGw.save(movDom));
        assertTrue(movGw.findById(1).isPresent());
        assertEquals(1, movGw.findAll().size());
        assertEquals(1, movGw.findByIdProducto("P1").size());
        assertEquals(1, movGw.findByFechaBetween(LocalDateTime.now(), LocalDateTime.now()).size());

        assertEquals(cierreDom, cierreGw.save(cierreDom));
        assertTrue(cierreGw.findById(1).isPresent());
        assertTrue(cierreGw.findByFechaCierre(LocalDate.now()).isPresent());
        assertTrue(cierreGw.existsByFechaCierre(LocalDate.now()));
        assertEquals(1, cierreGw.findAll().size());

        assertEquals(cliDom, cliGw.save(cliDom));
        assertTrue(cliGw.findById(1).isPresent());
        assertTrue(cliGw.findByDocumentoIdentidad("123").isPresent());
        assertEquals(1, cliGw.findAll().size());
        assertTrue(cliGw.existsById(1));

        assertTrue(mpGw.findById(1).isPresent());
        assertEquals(1, mpGw.findAll().size());
        assertTrue(mpGw.existsById(1));

        assertTrue(tmGw.findById(1).isPresent());
        assertTrue(tmGw.findByNombre("Ingreso").isPresent());
        assertEquals(1, tmGw.findAll().size());

        assertTrue(tdGw.findById(1).isPresent());
        assertEquals(1, tdGw.findAll().size());
    }

    @Test
    void catalogAnalyticsYNotificacionFeignAdaptersYRestMappersCubrenTodasLasRamas() {
        // Arrange
        CatalogFeignClient catalogClient = mock(CatalogFeignClient.class);
        CatalogFeignAdapter catalogAdapter = new CatalogFeignAdapter(catalogClient);

        when(catalogClient.validarStockBatch(anyList())).thenReturn(List.of(
                new ItemStockValidadoFeignDTO("P1", "Cuaderno", new BigDecimal("5000.00"), 10, 2, true)
        ));
        when(catalogClient.obtenerProductoPorId("P1")).thenReturn(
                new CatalogProductoFeignDTO("P1", "Cuaderno", new BigDecimal("5000.00"), 10, 2)
        );
        when(catalogClient.obtenerServicioPorId(1)).thenReturn(
                new CatalogServicioFeignDTO(1, "Copia", "Desc", new BigDecimal("500.00"))
        );

        AnalyticsFeignClient analyticsClient = mock(AnalyticsFeignClient.class);
        AnalyticsFeignAdapter analyticsAdapter = new AnalyticsFeignAdapter(analyticsClient);
        doThrow(new RuntimeException("Analytics down")).when(analyticsClient).sincronizarVenta(any());
        doThrow(new RuntimeException("Analytics down")).when(analyticsClient).sincronizarAnulacionVenta(any());

        NotificationFeignClient notifClient = mock(NotificationFeignClient.class);
        AuthFeignClient authClient = mock(AuthFeignClient.class);
        NotificacionFeignAdapter notifAdapter = new NotificacionFeignAdapter(notifClient, authClient);
        when(authClient.obtenerContactosAdministradores()).thenReturn(List.of(
                new ContactoUsuarioFeignDTO(1, "Admin", "admin@test.com")
        ));
        doThrow(new RuntimeException("SMTP down")).when(notifClient).enviarComprobanteVenta(any());
        doThrow(new RuntimeException("SMTP down")).when(notifClient).enviarResumenCierreCaja(any());

        Venta venta = new Venta(1, 1, LocalDateTime.now(),
                new Cliente(1, null, "100", "Ana", null, "Gil", null, "300", "ana@test.com"),
                new MetodoPago(1, "Efectivo"), new BigDecimal("5000.00"),
                Venta.ESTADO_COMPLETADA, null, null, null,
                List.of(new DetalleVenta(1, 1, "P1", TipoItem.PRODUCTO, "Cuaderno", new BigDecimal("5000.00"), 1, new BigDecimal("5000.00"))));

        CierreCaja cierre = new CierreCaja(1, LocalDate.now(), 1, new BigDecimal("5000.00"), 1,
                new BigDecimal("5000.00"), new BigDecimal("5000.00"), BigDecimal.ZERO, true, LocalDate.now().plusDays(1),
                "OK", LocalDateTime.now(), List.of(new CierreCajaDetalle(1, 1, new MetodoPago(1, "Efectivo"), new BigDecimal("5000.00"), 1)));

        // Act & Assert
        assertTrue(catalogAdapter.validarStockBatch(List.of()).isEmpty());
        assertEquals(1, catalogAdapter.validarStockBatch(List.of(new SolicitudStockItem("P1", 2))).size());
        assertDoesNotThrow(() -> catalogAdapter.descontarStockBatch(List.of()));
        assertDoesNotThrow(() -> catalogAdapter.descontarStockBatch(List.of(new SolicitudStockItem("P1", 2))));
        assertDoesNotThrow(() -> catalogAdapter.reintegrarStockBatch(List.of()));
        assertDoesNotThrow(() -> catalogAdapter.reintegrarStockBatch(List.of(new SolicitudStockItem("P1", 2))));
        assertTrue(catalogAdapter.obtenerProductoPorId("P1").isPresent());
        assertTrue(catalogAdapter.obtenerServicioPorId(1).isPresent());

        assertDoesNotThrow(() -> analyticsAdapter.sincronizarVenta(null));
        assertDoesNotThrow(() -> analyticsAdapter.sincronizarVenta(venta));
        assertDoesNotThrow(() -> analyticsAdapter.sincronizarAnulacionVenta(null));
        assertDoesNotThrow(() -> analyticsAdapter.sincronizarAnulacionVenta(venta));

        assertDoesNotThrow(() -> notifAdapter.enviarComprobanteVenta(null, null));
        assertDoesNotThrow(() -> notifAdapter.enviarComprobanteVenta(new Venta(), null));
        assertDoesNotThrow(() -> notifAdapter.enviarComprobanteVenta(venta, "custom@test.com"));
        assertDoesNotThrow(() -> notifAdapter.enviarResumenCierreCaja(null));
        assertDoesNotThrow(() -> notifAdapter.enviarResumenCierreCaja(cierre));
        when(authClient.obtenerContactosAdministradores()).thenReturn(List.of());
        assertDoesNotThrow(() -> notifAdapter.enviarResumenCierreCaja(cierre));

        Request req = Request.create(Request.HttpMethod.POST, "/internal/productos/descontar-stock", Map.of(), null, StandardCharsets.UTF_8, null);
        when(catalogClient.obtenerProductoPorId("404")).thenThrow(new FeignException.NotFound("404", req, null, null));
        when(catalogClient.obtenerServicioPorId(404)).thenThrow(new FeignException.NotFound("404", req, null, null));
        assertTrue(catalogAdapter.obtenerProductoPorId("404").isEmpty());
        assertTrue(catalogAdapter.obtenerServicioPorId(404).isEmpty());

        doThrow(new FeignException.NotFound("404", req, "No existe".getBytes(StandardCharsets.UTF_8), null))
                .when(catalogClient).descontarStockBatch(anyList());
        assertThrows(RecursoNoEncontradoException.class,
                () -> catalogAdapter.descontarStockBatch(List.of(new SolicitudStockItem("P1", 1))));

        doThrow(new FeignException.BadRequest("400", req, "Stock insuficiente".getBytes(StandardCharsets.UTF_8), null))
                .when(catalogClient).descontarStockBatch(anyList());
        assertThrows(StockInsuficienteException.class,
                () -> catalogAdapter.descontarStockBatch(List.of(new SolicitudStockItem("P1", 1))));

        doThrow(new FeignException.BadRequest("400", req, "Otra regla".getBytes(StandardCharsets.UTF_8), null))
                .when(catalogClient).descontarStockBatch(anyList());
        assertThrows(ReglaNegocioException.class,
                () -> catalogAdapter.descontarStockBatch(List.of(new SolicitudStockItem("P1", 1))));

        doThrow(new FeignException.ServiceUnavailable("503", req, "Down".getBytes(StandardCharsets.UTF_8), null))
                .when(catalogClient).descontarStockBatch(anyList());
        assertThrows(ServicioExternoNoDisponibleException.class,
                () -> catalogAdapter.descontarStockBatch(List.of(new SolicitudStockItem("P1", 1))));

        // Métodos default de los RestMappers
        VentaRestMapper vrm = Mappers.getMapper(VentaRestMapper.class);
        ClienteRestMapper crm = Mappers.getMapper(ClienteRestMapper.class);
        MovimientoInventarioRestMapper mrm = Mappers.getMapper(MovimientoInventarioRestMapper.class);

        assertNull(vrm.mapCliente(null));
        assertNotNull(vrm.mapCliente(1));
        assertNull(vrm.mapMetodoPago(null));
        assertNotNull(vrm.mapMetodoPago(1));
        assertNull(crm.mapTipoDocumento(null));
        assertNotNull(crm.mapTipoDocumento(1));
        assertNull(mrm.mapTipoMovimiento(null));
        assertNotNull(mrm.mapTipoMovimiento(1));
    }

    @Test
    void internalFeignRequestInterceptorInyectaApiKeyYPropagaAuthorization() {
        // Arrange
        InternalFeignRequestInterceptor interceptor = new InternalFeignRequestInterceptor("secret-internal-key");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-token-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
        RequestTemplate template = new RequestTemplate();

        // Act
        interceptor.apply(template);
        RequestContextHolder.resetRequestAttributes();
        new InternalFeignRequestInterceptor("").apply(new RequestTemplate());

        // Assert
        assertTrue(template.headers().get(InternalFeignRequestInterceptor.HEADER_INTERNAL_API_KEY).contains("secret-internal-key"));
        assertTrue(template.headers().get(HttpHeaders.AUTHORIZATION).contains("Bearer jwt-token-123"));
    }
}
