package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.EstadoProductoData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.EstadoProductoJpaRepository;
import com.invengest.catalog.infrastructure.mapper.EstadoProductoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadoProductoDataGatewayImplTest {

    @Mock
    private EstadoProductoJpaRepository estadoProductoJpaRepository;
    @Mock
    private EstadoProductoMapper estadoProductoMapper;

    private EstadoProductoDataGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new EstadoProductoDataGatewayImpl(estadoProductoJpaRepository, estadoProductoMapper);
    }

    @Test
    @DisplayName("Delega consultas de estados de producto en repositorio y mapper")
    void delegaConsultasEnRepositorioYMapper() {
        // Arrange
        EstadoProducto ep = new EstadoProducto(1, "Activo");
        EstadoProductoData epData = new EstadoProductoData(1, "Activo");
        when(estadoProductoJpaRepository.findById(1)).thenReturn(Optional.of(epData));
        when(estadoProductoJpaRepository.findAll()).thenReturn(List.of(epData));
        when(estadoProductoJpaRepository.findByNombreIgnoreCase("Activo")).thenReturn(Optional.of(epData));
        when(estadoProductoMapper.toDomain(epData)).thenReturn(ep);

        // Act
        Optional<EstadoProducto> porId = gateway.findById(1);
        List<EstadoProducto> todos = gateway.findAll();
        Optional<EstadoProducto> porNombre = gateway.findByNombre("Activo");

        // Assert
        assertTrue(porId.isPresent());
        assertEquals(1, todos.size());
        assertTrue(porNombre.isPresent());
    }
}
