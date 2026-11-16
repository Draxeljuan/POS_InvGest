package com.invengest.catalog;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.exception.ReglaNegocioException;
import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.MejorProveedorProducto;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.ProductoPrecioUsd;
import com.invengest.catalog.domain.model.ProductoProveedor;
import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.domain.model.SolicitudItemStock;
import com.invengest.catalog.domain.model.TasaCambio;
import com.invengest.catalog.domain.model.ValidacionItemStock;
import com.invengest.catalog.domain.model.gateway.AnalyticsGateway;
import com.invengest.catalog.domain.model.gateway.CategoriaGateway;
import com.invengest.catalog.domain.model.gateway.EstadoProductoGateway;
import com.invengest.catalog.domain.model.gateway.NotificacionStockGateway;
import com.invengest.catalog.domain.model.gateway.ProductoGateway;
import com.invengest.catalog.domain.model.gateway.ProductoProveedorGateway;
import com.invengest.catalog.domain.model.gateway.ProveedorGateway;
import com.invengest.catalog.domain.model.gateway.TasaCambioGateway;
import com.invengest.catalog.domain.usecase.ProductoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoUseCaseTest {

    @Mock
    private ProductoGateway productoGateway;
    @Mock
    private EstadoProductoGateway estadoProductoGateway;
    @Mock
    private CategoriaGateway categoriaGateway;
    @Mock
    private ProductoProveedorGateway productoProveedorGateway;
    @Mock
    private ProveedorGateway proveedorGateway;
    @Mock
    private TasaCambioGateway tasaCambioGateway;
    @Mock
    private AnalyticsGateway analyticsGateway;
    @Mock
    private NotificacionStockGateway notificacionStockGateway;

    private ProductoUseCase productoUseCase;

    @BeforeEach
    void setUp() {
        productoUseCase = new ProductoUseCase(
                productoGateway,
                estadoProductoGateway,
                categoriaGateway,
                productoProveedorGateway,
                proveedorGateway,
                tasaCambioGateway,
                analyticsGateway,
                notificacionStockGateway
        );
    }

    @Test
    void registrarGeneraIdSecuencialSinRandomYNotificaSiStockBajo() {
        // Arrange
        Categoria categoria = new Categoria(1, "Cuadernos", "Escolares");
        EstadoProducto estadoActivo = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        Producto nuevo = new Producto(
                null,
                new Categoria(1, null, null),
                "Cuaderno Argollado",
                new BigDecimal("12000.00"),
                new BigDecimal("7000.00"),
                LocalDate.of(2026, 11, 16),
                3,
                5,
                "Pasillo 1",
                null
        );

        when(categoriaGateway.findById(1)).thenReturn(Optional.of(categoria));
        when(estadoProductoGateway.findByNombre(EstadoProducto.ESTADO_ACTIVO)).thenReturn(Optional.of(estadoActivo));
        when(productoGateway.generarSiguienteIdProducto()).thenReturn("PROD-0001");
        when(productoGateway.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Producto registrado = productoUseCase.registrar(nuevo);

        // Assert
        assertEquals("PROD-0001", registrado.getIdProducto());
        assertEquals(EstadoProducto.ESTADO_ACTIVO, registrado.getEstado().getNombre());
        verify(analyticsGateway).notificarEstadoStock(registrado);
        verify(notificacionStockGateway).enviarAlertaStock(List.of(registrado));
    }

    @Test
    void registrarConSkuExplicitoYValidacionesDeCategoriaYEstado() {
        // Arrange
        Categoria categoria = new Categoria(1, "Cuadernos", "Escolares");
        EstadoProducto estadoActivo = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        Producto conSku = new Producto(
                " PROD-0010 ",
                new Categoria(1, null, null),
                "Cuaderno",
                new BigDecimal("9000.00"),
                new BigDecimal("5000.00"),
                LocalDate.now(),
                10,
                2,
                "A1",
                new EstadoProducto(1, null)
        );

        when(categoriaGateway.findById(1)).thenReturn(Optional.of(categoria));
        when(estadoProductoGateway.findById(1)).thenReturn(Optional.of(estadoActivo));
        when(productoGateway.existsById("PROD-0010")).thenReturn(false);
        when(productoGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Producto guardado = productoUseCase.registrar(conSku);

        // Assert
        assertEquals("PROD-0010", guardado.getIdProducto());

        when(productoGateway.existsById("PROD-0010")).thenReturn(true);
        assertThrows(EntidadDuplicadaException.class, () -> productoUseCase.registrar(conSku));
        assertThrows(ReglaNegocioException.class, () -> productoUseCase.registrar(null));

        Producto sinCategoria = new Producto(null, null, "Lápiz", BigDecimal.TEN, BigDecimal.ONE, LocalDate.now(), 5, 1, "A1", null);
        assertThrows(RecursoNoEncontradoException.class, () -> productoUseCase.registrar(sinCategoria));
    }

    @Test
    void actualizarYConsultasValidanCasosDeExitoYError() {
        // Arrange
        Categoria categoria = new Categoria(1, "Papelería", "General");
        EstadoProducto estadoActivo = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        Producto producto = new Producto(
                "PROD-0001",
                new Categoria(1, null, null),
                "Carpeta Plástica",
                new BigDecimal("4500.00"),
                new BigDecimal("2000.00"),
                LocalDate.now(),
                20,
                5,
                "Estante B",
                new EstadoProducto(1, null)
        );

        when(productoGateway.existsById("PROD-0001")).thenReturn(true);
        when(categoriaGateway.findById(1)).thenReturn(Optional.of(categoria));
        when(estadoProductoGateway.findById(1)).thenReturn(Optional.of(estadoActivo));
        when(productoGateway.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productoGateway.findAll()).thenReturn(List.of(producto));
        when(productoGateway.findBySearchCriteria("Carpeta")).thenReturn(List.of(producto));
        when(productoGateway.findLowStockProducts()).thenReturn(List.of(producto));

        // Act
        Producto actualizado = productoUseCase.actualizar(producto);
        List<Producto> todos = productoUseCase.obtenerTodos();
        List<Producto> busquedaVacia = productoUseCase.buscarPorCriterio(" ");
        List<Producto> busquedaCriterio = productoUseCase.buscarPorCriterio("Carpeta");
        List<Producto> stockBajo = productoUseCase.obtenerProductosStockBajo();

        // Assert
        assertEquals("PROD-0001", actualizado.getIdProducto());
        assertEquals(1, todos.size());
        assertEquals(1, busquedaVacia.size());
        assertEquals(1, busquedaCriterio.size());
        assertEquals(1, stockBajo.size());
        verify(analyticsGateway, never()).notificarEstadoStock(any());
        verify(notificacionStockGateway, never()).enviarAlertaStock(anyList());

        assertThrows(ReglaNegocioException.class, () -> productoUseCase.actualizar(null));
        when(productoGateway.existsById("PROD-9999")).thenReturn(false);
        producto.setIdProducto("PROD-9999");
        assertThrows(RecursoNoEncontradoException.class, () -> productoUseCase.actualizar(producto));
    }

    @Test
    void validarStockBatchRetornaDisponibilidadYPrecioVigenteEnUnaConsulta() {
        // Arrange
        EstadoProducto activo = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        Producto p1 = new Producto("PROD-0001", new Categoria(1, "General", ""), "Marcador",
                new BigDecimal("3500.00"), new BigDecimal("1800.00"), LocalDate.now(), 10, 2, "A1", activo);
        Producto p2 = new Producto("PROD-0002", new Categoria(1, "General", ""), "Borrador",
                new BigDecimal("1200.00"), new BigDecimal("600.00"), LocalDate.now(), 1, 2, "A2", activo);

        when(productoGateway.findByIds(List.of("PROD-0001", "PROD-0002"))).thenReturn(List.of(p1, p2));

        List<SolicitudItemStock> lote = List.of(
                new SolicitudItemStock("PROD-0001", 3),
                new SolicitudItemStock("PROD-0002", 5)
        );

        // Act
        List<ValidacionItemStock> validaciones = productoUseCase.validarStockBatch(lote);

        // Assert
        assertEquals(2, validaciones.size());
        assertTrue(validaciones.get(0).suficiente());
        assertEquals(new BigDecimal("3500.00"), validaciones.get(0).precioVenta());
        assertFalse(validaciones.get(1).suficiente());
        assertEquals(1, validaciones.get(1).stockDisponible());

        when(productoGateway.findByIds(List.of("PROD-404"))).thenReturn(List.of());
        assertThrows(RecursoNoEncontradoException.class,
                () -> productoUseCase.validarStockBatch(List.of(new SolicitudItemStock("PROD-404", 1))));
    }

    @Test
    void descontarStockBatchAtomicoDisparaAlertasAnalyticsYNotificationEnStockBajo() {
        // Arrange
        EstadoProducto estadoAgotado = new EstadoProducto(3, EstadoProducto.ESTADO_AGOTADO);
        Producto actualizadoEnAlerta = new Producto(
                "PROD-0001",
                new Categoria(1, "Cuadernos", ""),
                "Cuaderno 50 Hojas",
                new BigDecimal("5000.00"),
                new BigDecimal("3000.00"),
                LocalDate.now(),
                0,
                3,
                "A1",
                estadoAgotado
        );
        List<SolicitudItemStock> lote = List.of(new SolicitudItemStock("PROD-0001", 2));

        when(estadoProductoGateway.findByNombre(EstadoProducto.ESTADO_AGOTADO)).thenReturn(Optional.of(estadoAgotado));
        when(productoGateway.descontarStockAtomico(lote, estadoAgotado)).thenReturn(List.of(actualizadoEnAlerta));

        // Act
        Producto resultado = productoUseCase.descontarStock("PROD-0001", 2);

        // Assert
        assertEquals(0, resultado.getStock());
        verify(analyticsGateway).notificarEstadoStock(actualizadoEnAlerta);
        verify(notificacionStockGateway).enviarAlertaStock(List.of(actualizadoEnAlerta));
        assertThrows(ReglaNegocioException.class, () -> productoUseCase.descontarStockBatch(List.of()));
    }

    @Test
    void reintegrarStockBatchYEliminacionLogicaUsanConstantesDeDominio() {
        // Arrange
        EstadoProducto estadoActivo = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        EstadoProducto estadoDescontinuado = new EstadoProducto(2, EstadoProducto.ESTADO_DESCONTINUADO);
        Producto producto = new Producto(
                "PROD-0001",
                new Categoria(1, "General", ""),
                "Tijeras Escolares",
                new BigDecimal("4000.00"),
                new BigDecimal("2000.00"),
                LocalDate.now(),
                8,
                2,
                "B1",
                estadoActivo
        );
        List<SolicitudItemStock> lote = List.of(new SolicitudItemStock("PROD-0001", 3));

        when(estadoProductoGateway.findByNombre(EstadoProducto.ESTADO_ACTIVO)).thenReturn(Optional.of(estadoActivo));
        when(productoGateway.reintegrarStockAtomico(lote, estadoActivo)).thenReturn(List.of(producto));
        when(productoGateway.findById("PROD-0001")).thenReturn(Optional.of(producto));
        when(estadoProductoGateway.findByNombre(EstadoProducto.ESTADO_DESCONTINUADO))
                .thenReturn(Optional.of(estadoDescontinuado));

        // Act
        List<Producto> reintegrados = productoUseCase.reintegrarStockBatch(lote);
        productoUseCase.eliminacionLogica("PROD-0001");

        // Assert
        assertEquals(1, reintegrados.size());
        assertEquals(EstadoProducto.ESTADO_DESCONTINUADO, producto.getEstado().getNombre());
        verify(productoGateway).save(producto);
        assertThrows(ReglaNegocioException.class, () -> productoUseCase.reintegrarStockBatch(List.of()));
    }

    @Test
    void listarYConsultarPreciosUsdConvierteConTrmVigente() {
        // Arrange
        Producto producto = new Producto(
                "PROD-0001",
                new Categoria(1, "Papelería", ""),
                "Calculadora Científica",
                new BigDecimal("40000.00"),
                new BigDecimal("25000.00"),
                LocalDate.now(),
                5,
                2,
                "Vitrina",
                new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO)
        );
        TasaCambio trm = new TasaCambio(new BigDecimal("4000.00"), LocalDate.of(2026, 11, 16), TasaCambio.FUENTE_CACHE);

        when(tasaCambioGateway.obtenerTrmVigente()).thenReturn(trm);
        when(productoGateway.findAll()).thenReturn(List.of(producto));
        when(productoGateway.findById("PROD-0001")).thenReturn(Optional.of(producto));

        // Act
        List<ProductoPrecioUsd> lista = productoUseCase.listarPreciosEnUsd();
        ProductoPrecioUsd individual = productoUseCase.obtenerPrecioEnUsd("PROD-0001");

        // Assert
        assertEquals(1, lista.size());
        assertEquals(new BigDecimal("10.00"), individual.precioVentaUsd());
        assertEquals(TasaCambio.FUENTE_CACHE, individual.fuenteTrm());
    }

    @Test
    void obtenerMejorProveedorEligeProveedorActivoConMenorCostoYCalculaMargen() {
        // Arrange
        Producto producto = new Producto(
                "PROD-0001",
                new Categoria(1, "Cuadernos", ""),
                "Cuaderno Profesional",
                new BigDecimal("10000.00"),
                new BigDecimal("6000.00"),
                LocalDate.now(),
                15,
                3,
                "A1",
                new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO)
        );
        EstadoProveedor provActivo = new EstadoProveedor(1, EstadoProveedor.ESTADO_ACTIVO);
        EstadoProveedor provInactivo = new EstadoProveedor(2, EstadoProveedor.ESTADO_INACTIVO);

        Proveedor pInactivoMasBarato = new Proveedor(1, provInactivo, "Inactivo SAS", "111", "a@a.com", "Dir 1", "9001");
        Proveedor pActivoCaro = new Proveedor(2, provActivo, "Caro Ltda", "222", "b@b.com", "Dir 2", "9002");
        Proveedor pActivoMejor = new Proveedor(3, provActivo, "Editorial Norma", "333", "ventas@norma.com", "Dir 3", "9003");

        when(productoGateway.findById("PROD-0001")).thenReturn(Optional.of(producto));
        when(productoProveedorGateway.findByProductoId("PROD-0001")).thenReturn(List.of(
                new ProductoProveedor("PROD-0001", 1, new BigDecimal("4000.00")),
                new ProductoProveedor("PROD-0001", 2, new BigDecimal("7000.00")),
                new ProductoProveedor("PROD-0001", 3, new BigDecimal("5500.00"))
        ));
        when(proveedorGateway.findById(1)).thenReturn(Optional.of(pInactivoMasBarato));
        when(proveedorGateway.findById(2)).thenReturn(Optional.of(pActivoCaro));
        when(proveedorGateway.findById(3)).thenReturn(Optional.of(pActivoMejor));

        // Act
        MejorProveedorProducto mejor = productoUseCase.obtenerMejorProveedor("PROD-0001");

        // Assert
        assertEquals(3, mejor.idProveedor());
        assertEquals("Editorial Norma", mejor.nombreProveedor());
        assertEquals(new BigDecimal("5500.00"), mejor.precioCompraProveedor());
        assertEquals(new BigDecimal("4500.00"), mejor.margenUnitarioEstimado());
        assertEquals(new BigDecimal("45.00"), mejor.porcentajeMargenEstimado());
    }

    @Test
    void obtenerMejorProveedorLanza404CuandoNoHayProveedoresActivos() {
        // Arrange
        Producto producto = new Producto("PROD-0001", new Categoria(1, "C", ""), "P",
                BigDecimal.TEN, BigDecimal.ONE, LocalDate.now(), 5, 1, "A", new EstadoProducto(1, "Activo"));
        when(productoGateway.findById("PROD-0001")).thenReturn(Optional.of(producto));
        when(productoProveedorGateway.findByProductoId("PROD-0001")).thenReturn(List.of());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> productoUseCase.obtenerMejorProveedor("PROD-0001"));
        assertThrows(ReglaNegocioException.class, () -> productoUseCase.validarStockBatch(List.of()));
    }
}
