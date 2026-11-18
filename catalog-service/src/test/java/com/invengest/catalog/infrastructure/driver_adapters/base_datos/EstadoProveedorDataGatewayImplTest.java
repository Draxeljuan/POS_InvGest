package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.EstadoProveedorData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.EstadoProveedorJpaRepository;
import com.invengest.catalog.infrastructure.mapper.EstadoProveedorMapper;
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
class EstadoProveedorDataGatewayImplTest {

    @Mock
    private EstadoProveedorJpaRepository estadoProveedorJpaRepository;
    @Mock
    private EstadoProveedorMapper estadoProveedorMapper;

    private EstadoProveedorDataGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new EstadoProveedorDataGatewayImpl(estadoProveedorJpaRepository, estadoProveedorMapper);
    }

    @Test
    @DisplayName("Delega consultas de estados de proveedor en repositorio y mapper")
    void delegaConsultasEnRepositorioYMapper() {
        // Arrange
        EstadoProveedor epr = new EstadoProveedor(1, "Activo");
        EstadoProveedorData eprData = new EstadoProveedorData(1, "Activo");
        when(estadoProveedorJpaRepository.findById(1)).thenReturn(Optional.of(eprData));
        when(estadoProveedorJpaRepository.findAll()).thenReturn(List.of(eprData));
        when(estadoProveedorJpaRepository.findByNombreIgnoreCase("Activo")).thenReturn(Optional.of(eprData));
        when(estadoProveedorMapper.toDomain(eprData)).thenReturn(epr);

        // Act
        Optional<EstadoProveedor> porId = gateway.findById(1);
        List<EstadoProveedor> todos = gateway.findAll();
        Optional<EstadoProveedor> porNombre = gateway.findByNombre("Activo");

        // Assert
        assertTrue(porId.isPresent());
        assertEquals(1, todos.size());
        assertTrue(porNombre.isPresent());
    }
}
