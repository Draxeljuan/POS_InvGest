package com.invengest.catalog;

import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.ProductoProveedor;
import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.domain.model.Servicio;
import com.invengest.catalog.domain.model.SolicitudItemStock;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.CategoriaDataGatewayImpl;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.CategoriaServicioDataGatewayImpl;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.EstadoProductoDataGatewayImpl;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.EstadoProveedorDataGatewayImpl;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.ProductoDataGatewayImpl;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.ProductoProveedorDataGatewayImpl;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.ProveedorDataGatewayImpl;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.ServicioDataGatewayImpl;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.CategoriaData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.CategoriaServicioData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.EstadoProductoData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.EstadoProveedorData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProductoData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProductoProveedorData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProductoProveedorDataId;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProveedorData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ServicioData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.CategoriaJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.CategoriaServicioJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.EstadoProductoJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.EstadoProveedorJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ProductoJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ProductoProveedorJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ProveedorJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ServicioJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.feign.AnalyticsFeignAdapter;
import com.invengest.catalog.infrastructure.driver_adapters.feign.AnalyticsFeignClient;
import com.invengest.catalog.infrastructure.driver_adapters.feign.AuthFeignClient;
import com.invengest.catalog.infrastructure.driver_adapters.feign.InternalFeignRequestInterceptor;
import com.invengest.catalog.infrastructure.driver_adapters.feign.NotificacionStockFeignAdapter;
import com.invengest.catalog.infrastructure.driver_adapters.feign.NotificationFeignClient;
import com.invengest.catalog.infrastructure.driver_adapters.feign.dto.ContactoUsuarioDTO;
import com.invengest.catalog.infrastructure.mapper.CategoriaMapper;
import com.invengest.catalog.infrastructure.mapper.CategoriaServicioMapper;
import com.invengest.catalog.infrastructure.mapper.EstadoProductoMapper;
import com.invengest.catalog.infrastructure.mapper.EstadoProveedorMapper;
import com.invengest.catalog.infrastructure.mapper.ProductoMapper;
import com.invengest.catalog.infrastructure.mapper.ProductoProveedorMapper;
import com.invengest.catalog.infrastructure.mapper.ProveedorMapper;
import com.invengest.catalog.infrastructure.mapper.ServicioMapper;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogAdaptersTest {

    @Mock
    private ProductoJpaRepository productoJpaRepository;
    @Mock
    private ProductoMapper productoMapper;
    @Mock
    private CategoriaJpaRepository categoriaJpaRepository;
    @Mock
    private CategoriaMapper categoriaMapper;
    @Mock
    private CategoriaServicioJpaRepository categoriaServicioJpaRepository;
    @Mock
    private CategoriaServicioMapper categoriaServicioMapper;
    @Mock
    private EstadoProductoJpaRepository estadoProductoJpaRepository;
    @Mock
    private EstadoProductoMapper estadoProductoMapper;
    @Mock
    private EstadoProveedorJpaRepository estadoProveedorJpaRepository;
    @Mock
    private EstadoProveedorMapper estadoProveedorMapper;
    @Mock
    private ProveedorJpaRepository proveedorJpaRepository;
    @Mock
    private ProveedorMapper proveedorMapper;
    @Mock
    private ProductoProveedorJpaRepository productoProveedorJpaRepository;
    @Mock
    private ProductoProveedorMapper productoProveedorMapper;
    @Mock
    private ServicioJpaRepository servicioJpaRepository;
    @Mock
    private ServicioMapper servicioMapper;
    @Mock
    private AnalyticsFeignClient analyticsFeignClient;
    @Mock
    private AuthFeignClient authFeignClient;
    @Mock
    private NotificationFeignClient notificationFeignClient;

    @Test
    void productoDataGatewayImplEjecutaDescuentoYReintegroAtomicoYConsultas() {
        // Arrange
        ProductoDataGatewayImpl gateway = new ProductoDataGatewayImpl(productoJpaRepository, productoMapper);
        EstadoProducto activo = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        EstadoProducto agotado = new EstadoProducto(3, EstadoProducto.ESTADO_AGOTADO);
        Producto domain = new Producto("PROD-0001", new Categoria(1, "C", ""), "Cuaderno",
                new BigDecimal("5000.00"), new BigDecimal("3000.00"), LocalDate.now(), 2, 1, "A1", activo);
        ProductoData entity = new ProductoData();
        entity.setIdProducto("PROD-0001");

        when(productoJpaRepository.findById("PROD-0001")).thenReturn(Optional.of(entity));
        when(productoJpaRepository.findAllById(List.of("PROD-0001"))).thenReturn(List.of(entity));
        when(productoJpaRepository.findAll()).thenReturn(List.of(entity));
        when(productoJpaRepository.searchByCriteria("Cuaderno")).thenReturn(List.of(entity));
        when(productoJpaRepository.findLowStockProducts()).thenReturn(List.of(entity));
        when(productoJpaRepository.save(entity)).thenReturn(entity);
        when(productoMapper.toDomain(entity)).thenReturn(domain);
        when(productoMapper.toEntity(any(Producto.class))).thenReturn(entity);
        when(productoJpaRepository.saveAll(anyList())).thenReturn(List.of(entity));
        when(productoJpaRepository.count()).thenReturn(0L);
        when(productoJpaRepository.existsById("PROD-0001")).thenReturn(false);

        // Act
        String siguienteId = gateway.generarSiguienteIdProducto();
        Optional<Producto> porId = gateway.findById("PROD-0001");
        List<Producto> porIds = gateway.findByIds(List.of("PROD-0001"));
        List<Producto> todos = gateway.findAll();
        List<Producto> porCriterio = gateway.findBySearchCriteria("Cuaderno");
        List<Producto> stockBajo = gateway.findLowStockProducts();
        Producto guardado = gateway.save(domain);
        boolean existe = gateway.existsById("PROD-0001");
        List<Producto> descontados = gateway.descontarStockAtomico(
                List.of(new SolicitudItemStock("PROD-0001", 2)), agotado);
        List<Producto> reintegrados = gateway.reintegrarStockAtomico(
                List.of(new SolicitudItemStock("PROD-0001", 5)), activo);

        when(productoJpaRepository.existsById("PROD-0001")).thenReturn(true);
        String idColisionFallback = gateway.generarSiguienteIdProducto();

        // Assert
        assertEquals("PROD-0001", siguienteId);
        assertTrue(idColisionFallback.startsWith("PROD-"));
        assertTrue(porId.isPresent());
        assertEquals(1, porIds.size());
        assertEquals(1, todos.size());
        assertEquals(1, porCriterio.size());
        assertEquals(1, stockBajo.size());
        assertEquals("PROD-0001", guardado.getIdProducto());
        assertFalse(existe);
        assertEquals(1, descontados.size());
        assertEquals(5, reintegrados.getFirst().getStock());
        assertEquals(EstadoProducto.ESTADO_ACTIVO, reintegrados.getFirst().getEstado().getNombre());

        when(productoJpaRepository.findAllById(List.of("PROD-9999"))).thenReturn(List.of());
        assertThrows(RecursoNoEncontradoException.class, () ->
                gateway.descontarStockAtomico(List.of(new SolicitudItemStock("PROD-9999", 1)), agotado));
    }

    @Test
    void adaptadoresJpaSecundariosDeleganEnRepositoriosYMappers() {
        // Arrange
        CategoriaDataGatewayImpl catGw = new CategoriaDataGatewayImpl(categoriaJpaRepository, productoJpaRepository, categoriaMapper);
        CategoriaServicioDataGatewayImpl catServGw = new CategoriaServicioDataGatewayImpl(categoriaServicioJpaRepository, servicioJpaRepository, categoriaServicioMapper);
        EstadoProductoDataGatewayImpl estProdGw = new EstadoProductoDataGatewayImpl(estadoProductoJpaRepository, estadoProductoMapper);
        EstadoProveedorDataGatewayImpl estProvGw = new EstadoProveedorDataGatewayImpl(estadoProveedorJpaRepository, estadoProveedorMapper);
        ProveedorDataGatewayImpl provGw = new ProveedorDataGatewayImpl(proveedorJpaRepository, productoProveedorJpaRepository, proveedorMapper);
        ProductoProveedorDataGatewayImpl prodProvGw = new ProductoProveedorDataGatewayImpl(productoProveedorJpaRepository, productoProveedorMapper);
        ServicioDataGatewayImpl servGw = new ServicioDataGatewayImpl(servicioJpaRepository, servicioMapper);

        Categoria cat = new Categoria(1, "C", "D");
        CategoriaData catData = new CategoriaData(1, "C", "D");
        when(categoriaMapper.toEntity(cat)).thenReturn(catData);
        when(categoriaJpaRepository.save(catData)).thenReturn(catData);
        when(categoriaMapper.toDomain(catData)).thenReturn(cat);
        when(categoriaJpaRepository.findById(1)).thenReturn(Optional.of(catData));
        when(categoriaJpaRepository.findAll()).thenReturn(List.of(catData));
        when(categoriaJpaRepository.existsByNombreIgnoreCase("C")).thenReturn(true);
        when(productoJpaRepository.existsByCategoria_IdCategoria(1)).thenReturn(false);

        CategoriaServicio cs = new CategoriaServicio(1, "Deshabilitados");
        CategoriaServicioData csData = new CategoriaServicioData(1, "Deshabilitados");
        when(categoriaServicioMapper.toEntity(cs)).thenReturn(csData);
        when(categoriaServicioJpaRepository.save(csData)).thenReturn(csData);
        when(categoriaServicioJpaRepository.findById(1)).thenReturn(Optional.of(csData));
        when(categoriaServicioJpaRepository.findAll()).thenReturn(List.of(csData));
        when(categoriaServicioJpaRepository.findByNombreIgnoreCase("Deshabilitados")).thenReturn(Optional.of(csData));
        when(categoriaServicioMapper.toDomain(csData)).thenReturn(cs);
        when(servicioJpaRepository.existsByCategoriaServicio_IdCategoriaServicio(1)).thenReturn(true);

        EstadoProducto ep = new EstadoProducto(1, "Activo");
        EstadoProductoData epData = new EstadoProductoData(1, "Activo");
        when(estadoProductoJpaRepository.findById(1)).thenReturn(Optional.of(epData));
        when(estadoProductoJpaRepository.findAll()).thenReturn(List.of(epData));
        when(estadoProductoJpaRepository.findByNombreIgnoreCase("Activo")).thenReturn(Optional.of(epData));
        when(estadoProductoMapper.toDomain(epData)).thenReturn(ep);

        EstadoProveedor epr = new EstadoProveedor(1, "Activo");
        EstadoProveedorData eprData = new EstadoProveedorData(1, "Activo");
        when(estadoProveedorJpaRepository.findById(1)).thenReturn(Optional.of(eprData));
        when(estadoProveedorJpaRepository.findAll()).thenReturn(List.of(eprData));
        when(estadoProveedorJpaRepository.findByNombreIgnoreCase("Activo")).thenReturn(Optional.of(eprData));
        when(estadoProveedorMapper.toDomain(eprData)).thenReturn(epr);

        Proveedor prov = new Proveedor(1, epr, "P", "1", "a@a.com", "D", "900");
        ProveedorData provData = new ProveedorData(1, eprData, "P", "1", "a@a.com", "D", "900");
        when(proveedorMapper.toEntity(prov)).thenReturn(provData);
        when(proveedorJpaRepository.save(provData)).thenReturn(provData);
        when(proveedorJpaRepository.findById(1)).thenReturn(Optional.of(provData));
        when(proveedorJpaRepository.findAll()).thenReturn(List.of(provData));
        when(proveedorJpaRepository.findByNit("900")).thenReturn(Optional.of(provData));
        when(proveedorMapper.toDomain(provData)).thenReturn(prov);
        when(productoProveedorJpaRepository.existsById_IdProveedor(1)).thenReturn(true);

        ProductoProveedor pp = new ProductoProveedor("PROD-0001", 1, BigDecimal.TEN);
        ProductoProveedorData ppData = new ProductoProveedorData(new ProductoProveedorDataId("PROD-0001", 1), BigDecimal.TEN);
        when(productoProveedorMapper.toEntity(pp)).thenReturn(ppData);
        when(productoProveedorJpaRepository.save(ppData)).thenReturn(ppData);
        when(productoProveedorJpaRepository.findById_IdProducto("PROD-0001")).thenReturn(List.of(ppData));
        when(productoProveedorJpaRepository.findById_IdProveedor(1)).thenReturn(List.of(ppData));
        when(productoProveedorMapper.toDomain(ppData)).thenReturn(pp);

        Servicio serv = new Servicio(1, cs, "S", "D", BigDecimal.TEN);
        ServicioData servData = new ServicioData(1, csData, "S", "D", BigDecimal.TEN);
        when(servicioMapper.toEntity(serv)).thenReturn(servData);
        when(servicioJpaRepository.save(servData)).thenReturn(servData);
        when(servicioJpaRepository.findById(1)).thenReturn(Optional.of(servData));
        when(servicioJpaRepository.findAll()).thenReturn(List.of(servData));
        when(servicioJpaRepository.existsByNombreIgnoreCase("S")).thenReturn(true);
        when(servicioJpaRepository.findByCategoriaServicio_IdCategoriaServicio(1)).thenReturn(List.of(servData));
        when(servicioMapper.toDomain(servData)).thenReturn(serv);

        // Act & Assert
        assertEquals("C", catGw.save(cat).getNombre());
        assertTrue(catGw.findById(1).isPresent());
        assertEquals(1, catGw.findAll().size());
        assertTrue(catGw.existsByNombre("C"));
        catGw.deleteById(1);
        assertFalse(catGw.existsProductosByCategoriaId(1));

        assertEquals("Deshabilitados", catServGw.save(cs).getNombre());
        assertTrue(catServGw.findById(1).isPresent());
        assertEquals(1, catServGw.findAll().size());
        catServGw.deleteById(1);
        assertTrue(catServGw.findByNombre("Deshabilitados").isPresent());
        assertTrue(catServGw.existsServiciosByCategoriaServicioId(1));

        assertTrue(estProdGw.findById(1).isPresent());
        assertEquals(1, estProdGw.findAll().size());
        assertTrue(estProdGw.findByNombre("Activo").isPresent());

        assertTrue(estProvGw.findById(1).isPresent());
        assertEquals(1, estProvGw.findAll().size());
        assertTrue(estProvGw.findByNombre("Activo").isPresent());

        assertEquals("P", provGw.save(prov).getNombre());
        assertTrue(provGw.findById(1).isPresent());
        assertEquals(1, provGw.findAll().size());
        assertTrue(provGw.findByNit("900").isPresent());
        assertTrue(provGw.existsProductosByProveedorId(1));

        assertEquals("PROD-0001", prodProvGw.save(pp).getIdProducto());
        assertEquals(1, prodProvGw.findByProductoId("PROD-0001").size());
        assertEquals(1, prodProvGw.findByProveedorId(1).size());
        prodProvGw.delete("PROD-0001", 1);

        assertEquals("S", servGw.save(serv).getNombre());
        assertTrue(servGw.findById(1).isPresent());
        assertEquals(1, servGw.findAll().size());
        assertTrue(servGw.existsByNombre("S"));
        assertEquals(1, servGw.findByCategoriaServicioId(1).size());
    }

    @Test
    void adaptadoresFeignAislanFallosEnTryCatchEInyectanInternalApiKey() {
        // Arrange
        AnalyticsFeignAdapter analyticsAdapter = new AnalyticsFeignAdapter(analyticsFeignClient);
        NotificacionStockFeignAdapter notifAdapter = new NotificacionStockFeignAdapter(authFeignClient, notificationFeignClient);
        InternalFeignRequestInterceptor interceptor = new InternalFeignRequestInterceptor("secret-internal-key");
        InternalFeignRequestInterceptor emptyInterceptor = new InternalFeignRequestInterceptor(" ");

        Producto prod = new Producto("PROD-0001", new Categoria(1, "C", ""), "Cuaderno",
                BigDecimal.TEN, BigDecimal.ONE, LocalDate.now(), 0, 3, "A1", new EstadoProducto(3, "Agotado"));

        when(authFeignClient.obtenerContactosAdministradores())
                .thenReturn(List.of(new ContactoUsuarioDTO(1, "Admin Principal", "admin@invengest.local")));
        doThrow(new RuntimeException("Analytics caído")).when(analyticsFeignClient).sincronizarAlertaStock(any());
        doThrow(new RuntimeException("Notification caído")).when(notificationFeignClient).enviarAlertaStock(any());

        RequestTemplate requestTemplate = new RequestTemplate();
        RequestTemplate emptyTemplate = new RequestTemplate();

        // Act & Assert
        assertDoesNotThrow(() -> analyticsAdapter.notificarEstadoStock(null));
        assertDoesNotThrow(() -> analyticsAdapter.notificarEstadoStock(prod));
        assertDoesNotThrow(() -> notifAdapter.enviarAlertaStock(List.of()));
        assertDoesNotThrow(() -> notifAdapter.enviarAlertaStock(List.of(prod)));

        when(authFeignClient.obtenerContactosAdministradores()).thenReturn(List.of());
        assertDoesNotThrow(() -> notifAdapter.enviarAlertaStock(List.of(prod)));

        when(authFeignClient.obtenerContactosAdministradores())
                .thenReturn(List.of(new ContactoUsuarioDTO(1, "Sin Correo", " ")));
        assertDoesNotThrow(() -> notifAdapter.enviarAlertaStock(List.of(prod)));

        interceptor.apply(requestTemplate);
        emptyInterceptor.apply(emptyTemplate);

        assertTrue(requestTemplate.headers().containsKey(InternalFeignRequestInterceptor.HEADER_INTERNAL_API_KEY));
        assertFalse(emptyTemplate.headers().containsKey(InternalFeignRequestInterceptor.HEADER_INTERNAL_API_KEY));
        verify(analyticsFeignClient).sincronizarAlertaStock(any());
        verify(notificationFeignClient).enviarAlertaStock(any());
    }
}
