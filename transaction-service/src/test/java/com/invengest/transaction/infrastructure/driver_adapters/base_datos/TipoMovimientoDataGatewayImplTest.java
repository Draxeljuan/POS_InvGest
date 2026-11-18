package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.TipoMovimientoData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.TipoMovimientoJpaRepository;
import com.invengest.transaction.infrastructure.mapper.TipoMovimientoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoMovimientoDataGatewayImplTest {

    @Mock
    private TipoMovimientoJpaRepository repository;
    @Mock
    private TipoMovimientoMapper mapper;

    @InjectMocks
    private TipoMovimientoDataGatewayImpl gateway;

    @Test
    @DisplayName("Debe consultar tipos de movimiento por ID, nombre y listado completo")
    void debeConsultarTiposMovimiento() {
        // Arrange
        TipoMovimiento tmDom = new TipoMovimiento(1, "Ingreso");
        TipoMovimientoData tmEnt = new TipoMovimientoData(1, "Ingreso");
        when(repository.findById(1)).thenReturn(Optional.of(tmEnt));
        when(repository.findByNombre("Ingreso")).thenReturn(Optional.of(tmEnt));
        when(repository.findAll()).thenReturn(List.of(tmEnt));
        when(mapper.toDomain(tmEnt)).thenReturn(tmDom);

        // Act
        Optional<TipoMovimiento> porId = gateway.findById(1);
        Optional<TipoMovimiento> porNombre = gateway.findByNombre("Ingreso");
        List<TipoMovimiento> todos = gateway.findAll();

        // Assert
        assertTrue(porId.isPresent());
        assertTrue(porNombre.isPresent());
        assertEquals(1, todos.size());
    }
}
