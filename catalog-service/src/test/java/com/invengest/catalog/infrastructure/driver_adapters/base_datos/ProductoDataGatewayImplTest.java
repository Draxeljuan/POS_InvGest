package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.SolicitudItemStock;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProductoData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ProductoJpaRepository;
import com.invengest.catalog.infrastructure.mapper.ProductoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoDataGatewayImplTest {

    @Mock
    private ProductoJpaRepository productoJpaRepository;
    @Mock
    private ProductoMapper productoMapper;

    private ProductoDataGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new ProductoDataGatewayImpl(productoJpaRepository, productoMapper);
    }

    @Test
    @DisplayName("Ejecuta descuento y reintegro atómico de stock y consultas de productos")
    void ejecutaDescuentoYReintegroAtomicoYConsultas() {
        // Arrange
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
        assertThrows(RecursoNoEncontradoException.class, () ->
                gateway.reintegrarStockAtomico(List.of(new SolicitudItemStock("PROD-9999", 1)), activo));
    }
}
