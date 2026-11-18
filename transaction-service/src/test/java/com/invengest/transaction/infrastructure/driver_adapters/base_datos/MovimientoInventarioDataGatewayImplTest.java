package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.MovimientoInventarioData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.MovimientoInventarioJpaRepository;
import com.invengest.transaction.infrastructure.mapper.MovimientoInventarioMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoInventarioDataGatewayImplTest {

    @Mock
    private MovimientoInventarioJpaRepository repository;
    @Mock
    private MovimientoInventarioMapper mapper;

    @InjectMocks
    private MovimientoInventarioDataGatewayImpl gateway;

    @Test
    @DisplayName("Debe persistir y consultar movimientos de inventario")
    void debePersistirYConsultarMovimientos() {
        // Arrange
        MovimientoInventario movDom = new MovimientoInventario();
        MovimientoInventarioData movEnt = new MovimientoInventarioData();
        when(mapper.toEntity(movDom)).thenReturn(movEnt);
        when(repository.save(movEnt)).thenReturn(movEnt);
        when(mapper.toDomain(movEnt)).thenReturn(movDom);
        when(repository.findById(1)).thenReturn(Optional.of(movEnt));
        when(repository.findAll()).thenReturn(List.of(movEnt));
        when(repository.findByIdProductoOrderByFechaMovimientoDesc("P1")).thenReturn(List.of(movEnt));
        when(repository.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(any(), any())).thenReturn(List.of(movEnt));

        // Act
        MovimientoInventario guardado = gateway.save(movDom);
        Optional<MovimientoInventario> porId = gateway.findById(1);
        List<MovimientoInventario> todos = gateway.findAll();
        List<MovimientoInventario> porProd = gateway.findByIdProducto("P1");
        List<MovimientoInventario> porFecha = gateway.findByFechaBetween(LocalDateTime.now(), LocalDateTime.now());

        // Assert
        assertEquals(movDom, guardado);
        assertTrue(porId.isPresent());
        assertEquals(1, todos.size());
        assertEquals(1, porProd.size());
        assertEquals(1, porFecha.size());
    }
}
