package com.invengest.catalog;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.EntidadEnUsoException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.ProductoProveedor;
import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.domain.model.Servicio;
import com.invengest.catalog.domain.model.gateway.CategoriaGateway;
import com.invengest.catalog.domain.model.gateway.CategoriaServicioGateway;
import com.invengest.catalog.domain.model.gateway.EstadoProductoGateway;
import com.invengest.catalog.domain.model.gateway.EstadoProveedorGateway;
import com.invengest.catalog.domain.model.gateway.ProductoProveedorGateway;
import com.invengest.catalog.domain.model.gateway.ProveedorGateway;
import com.invengest.catalog.domain.model.gateway.ServicioGateway;
import com.invengest.catalog.domain.usecase.CategoriaServicioUseCase;
import com.invengest.catalog.domain.usecase.CategoriaUseCase;
import com.invengest.catalog.domain.usecase.EstadoProductoUseCase;
import com.invengest.catalog.domain.usecase.EstadoProveedorUseCase;
import com.invengest.catalog.domain.usecase.ProductoProveedorUseCase;
import com.invengest.catalog.domain.usecase.ProveedorUseCase;
import com.invengest.catalog.domain.usecase.ServicioUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogSecondaryUseCasesTest {

    @Mock
    private CategoriaGateway categoriaGateway;
    @Mock
    private CategoriaServicioGateway categoriaServicioGateway;
    @Mock
    private EstadoProductoGateway estadoProductoGateway;
    @Mock
    private EstadoProveedorGateway estadoProveedorGateway;
    @Mock
    private ProveedorGateway proveedorGateway;
    @Mock
    private ProductoProveedorGateway productoProveedorGateway;
    @Mock
    private ServicioGateway servicioGateway;

    @Test
    void categoriaUseCaseGestionaCrudYValidaDuplicadosYProductosAsociados() {
        // Arrange
        CategoriaUseCase useCase = new CategoriaUseCase(categoriaGateway);
        Categoria nueva = new Categoria(null, "Escolar", "Útiles");
        Categoria guardada = new Categoria(1, "Escolar", "Útiles");
        Categoria actualizada = new Categoria(1, "Oficina", "Artículos");

        when(categoriaGateway.existsByNombre("Escolar")).thenReturn(false);
        when(categoriaGateway.existsByNombre("Oficina")).thenReturn(false);
        when(categoriaGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoriaGateway.findById(1)).thenReturn(Optional.of(guardada));
        when(categoriaGateway.findAll()).thenReturn(List.of(guardada));
        when(categoriaGateway.existsProductosByCategoriaId(1)).thenReturn(true);

        // Act
        Categoria creada = useCase.crear(nueva);
        Categoria mod = useCase.actualizar(actualizada);
        Categoria consultada = useCase.obtenerPorId(1);
        List<Categoria> todas = useCase.obtenerTodas();

        // Assert
        assertEquals("Escolar", creada.getNombre());
        assertEquals("Oficina", mod.getNombre());
        assertEquals("Escolar", consultada.getNombre());
        assertEquals(1, todas.size());
        assertThrows(EntidadEnUsoException.class, () -> useCase.eliminarPorId(1));

        when(categoriaGateway.existsProductosByCategoriaId(1)).thenReturn(false);
        useCase.eliminarPorId(1);
        verify(categoriaGateway).deleteById(1);

        when(categoriaGateway.existsByNombre("Escolar")).thenReturn(true);
        assertThrows(EntidadDuplicadaException.class, () -> useCase.crear(nueva));
    }

    @Test
    void proveedorUseCaseEliminacionLogicaUsaEstadoInactivoPorNombreSinMagicNumbers() {
        // Arrange
        ProveedorUseCase useCase = new ProveedorUseCase(proveedorGateway, estadoProveedorGateway);
        EstadoProveedor activo = new EstadoProveedor(1, EstadoProveedor.ESTADO_ACTIVO);
        EstadoProveedor inactivo = new EstadoProveedor(2, EstadoProveedor.ESTADO_INACTIVO);
        Proveedor proveedor = new Proveedor(10, activo, "Norma", "300000", "info@norma.com", "Calle 1", "900111");
        Proveedor sinEstado = new Proveedor(10, null, "Norma", "300000", "info@norma.com", "Calle 1", "900111");

        when(proveedorGateway.findByNit("900111")).thenReturn(Optional.empty());
        when(estadoProveedorGateway.findById(1)).thenReturn(Optional.of(activo));
        when(estadoProveedorGateway.findByNombre(EstadoProveedor.ESTADO_ACTIVO)).thenReturn(Optional.of(activo));
        when(proveedorGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(proveedorGateway.findById(10)).thenReturn(Optional.of(proveedor));
        when(proveedorGateway.findAll()).thenReturn(List.of(proveedor));
        when(proveedorGateway.existsProductosByProveedorId(10)).thenReturn(false);
        when(estadoProveedorGateway.findByNombre(EstadoProveedor.ESTADO_INACTIVO)).thenReturn(Optional.of(inactivo));

        // Act
        Proveedor creado = useCase.crear(proveedor);
        Proveedor actualizado = useCase.actualizar(sinEstado);
        Proveedor porId = useCase.obtenerPorId(10);
        List<Proveedor> todos = useCase.obtenerTodos();
        useCase.eliminacionLogica(10);

        // Assert
        assertEquals("Norma", creado.getNombre());
        assertEquals("Norma", actualizado.getNombre());
        assertEquals(10, porId.getIdProveedor());
        assertEquals(1, todos.size());
        assertEquals(EstadoProveedor.ESTADO_INACTIVO, proveedor.getEstado().getNombre());

        when(proveedorGateway.existsProductosByProveedorId(10)).thenReturn(true);
        assertThrows(EntidadEnUsoException.class, () -> useCase.eliminacionLogica(10));

        when(proveedorGateway.findByNit("900111")).thenReturn(Optional.of(proveedor));
        assertThrows(EntidadDuplicadaException.class, () -> useCase.crear(proveedor));
    }

    @Test
    void servicioUseCaseEliminacionLogicaUsaCategoriaDeshabilitadosPorNombre() {
        // Arrange
        ServicioUseCase useCase = new ServicioUseCase(servicioGateway, categoriaServicioGateway);
        CategoriaServicio catImpresion = new CategoriaServicio(2, "Redacción y Documentos");
        CategoriaServicio catDeshabilitados = new CategoriaServicio(1, CategoriaServicio.CATEGORIA_DESHABILITADOS);
        Servicio servicio = new Servicio(5, catImpresion, "Anillado", "Anillado plástico", new BigDecimal("3500.00"));
        Servicio servicioMod = new Servicio(5, catImpresion, "Anillado Espiral", "Plástico", new BigDecimal("4000.00"));

        when(servicioGateway.existsByNombre("Anillado")).thenReturn(false);
        when(servicioGateway.existsByNombre("Anillado Espiral")).thenReturn(false);
        when(categoriaServicioGateway.findById(2)).thenReturn(Optional.of(catImpresion));
        when(servicioGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(servicioGateway.findById(5)).thenReturn(Optional.of(servicio));
        when(servicioGateway.findAll()).thenReturn(List.of(servicio));
        when(servicioGateway.findByCategoriaServicioId(2)).thenReturn(List.of(servicio));
        when(categoriaServicioGateway.findByNombre(CategoriaServicio.CATEGORIA_DESHABILITADOS))
                .thenReturn(Optional.of(catDeshabilitados));

        // Act
        Servicio registrado = useCase.registrar(servicio);
        Servicio actualizado = useCase.actualizar(servicioMod);
        Servicio porId = useCase.obtenerPorId(5);
        List<Servicio> todos = useCase.obtenerTodos();
        List<Servicio> porCat = useCase.obtenerPorCategoria(2);
        useCase.eliminacionLogica(5);

        // Assert
        assertEquals("Anillado", registrado.getNombre());
        assertEquals("Anillado Espiral", actualizado.getNombre());
        assertEquals(5, porId.getIdServicio());
        assertEquals(1, todos.size());
        assertEquals(1, porCat.size());
        assertEquals(CategoriaServicio.CATEGORIA_DESHABILITADOS, servicio.getCategoriaServicio().getNombre());

        when(servicioGateway.existsByNombre("Anillado")).thenReturn(true);
        assertThrows(EntidadDuplicadaException.class, () -> useCase.registrar(servicio));
    }

    @Test
    void categoriaServicioEstadosYProductoProveedorUseCasesCubrenOperaciones() {
        // Arrange
        CategoriaServicioUseCase catServUC = new CategoriaServicioUseCase(categoriaServicioGateway);
        EstadoProductoUseCase estProdUC = new EstadoProductoUseCase(estadoProductoGateway);
        EstadoProveedorUseCase estProvUC = new EstadoProveedorUseCase(estadoProveedorGateway);
        ProductoProveedorUseCase prodProvUC = new ProductoProveedorUseCase(productoProveedorGateway);

        CategoriaServicio cs = new CategoriaServicio(1, "Trámites Digitales");
        EstadoProducto ep = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        EstadoProveedor epr = new EstadoProveedor(1, EstadoProveedor.ESTADO_ACTIVO);
        ProductoProveedor pp = new ProductoProveedor("PROD-0001", 1, new BigDecimal("5000.00"));

        when(categoriaServicioGateway.save(any())).thenReturn(cs);
        when(categoriaServicioGateway.findById(1)).thenReturn(Optional.of(cs));
        when(categoriaServicioGateway.findAll()).thenReturn(List.of(cs));
        when(categoriaServicioGateway.existsServiciosByCategoriaServicioId(1)).thenReturn(false);
        when(estadoProductoGateway.findAll()).thenReturn(List.of(ep));
        when(estadoProductoGateway.findById(1)).thenReturn(Optional.of(ep));
        when(estadoProductoGateway.findByNombre("Activo")).thenReturn(Optional.of(ep));
        when(estadoProveedorGateway.findAll()).thenReturn(List.of(epr));
        when(estadoProveedorGateway.findById(1)).thenReturn(Optional.of(epr));
        when(estadoProveedorGateway.findByNombre("Activo")).thenReturn(Optional.of(epr));
        when(productoProveedorGateway.save(any())).thenReturn(pp);
        when(productoProveedorGateway.findByProductoId("PROD-0001")).thenReturn(List.of(pp));
        when(productoProveedorGateway.findByProveedorId(1)).thenReturn(List.of(pp));

        // Act
        CategoriaServicio creadaCs = catServUC.crear(cs);
        catServUC.actualizar(cs);
        CategoriaServicio porIdCs = catServUC.obtenerPorId(1);
        List<CategoriaServicio> todasCs = catServUC.obtenerTodas();
        catServUC.eliminarPorId(1);
        EstadoProducto epRes = estProdUC.obtenerPorNombre("Activo");
        EstadoProveedor eprRes = estProvUC.obtenerPorNombre("Activo");
        ProductoProveedor ppRes = prodProvUC.asociarOActualizar(pp);
        prodProvUC.eliminarAsociacion("PROD-0001", 1);

        // Assert
        assertEquals("Trámites Digitales", creadaCs.getNombre());
        assertEquals(1, porIdCs.getIdCategoriaServicio());
        assertEquals(1, todasCs.size());
        assertEquals(1, estProdUC.obtenerTodos().size());
        assertEquals(1, estProdUC.obtenerPorId(1).getIdEstado());
        assertEquals("Activo", epRes.getNombre());
        assertEquals(1, estProvUC.obtenerTodos().size());
        assertEquals(1, estProvUC.obtenerPorId(1).getIdEstadoProveedor());
        assertEquals("Activo", eprRes.getNombre());
        assertEquals("PROD-0001", ppRes.getIdProducto());
        assertEquals(1, prodProvUC.obtenerPorProducto("PROD-0001").size());
        assertEquals(1, prodProvUC.obtenerPorProveedor(1).size());
        assertThrows(RecursoNoEncontradoException.class, () -> estProdUC.obtenerPorId(99));

        when(categoriaServicioGateway.existsServiciosByCategoriaServicioId(1)).thenReturn(true);
        assertThrows(EntidadEnUsoException.class, () -> catServUC.eliminarPorId(1));
    }
}
