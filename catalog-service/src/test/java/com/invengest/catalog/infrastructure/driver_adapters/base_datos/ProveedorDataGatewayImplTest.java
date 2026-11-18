package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.EstadoProveedorData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProveedorData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ProductoProveedorJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ProveedorJpaRepository;
import com.invengest.catalog.infrastructure.mapper.ProveedorMapper;
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
class ProveedorDataGatewayImplTest {

    @Mock
    private ProveedorJpaRepository proveedorJpaRepository;
    @Mock
    private ProductoProveedorJpaRepository productoProveedorJpaRepository;
    @Mock
    private ProveedorMapper proveedorMapper;

    private ProveedorDataGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new ProveedorDataGatewayImpl(proveedorJpaRepository, productoProveedorJpaRepository, proveedorMapper);
    }

    @Test
    @DisplayName("Delega operaciones de persistencia de proveedores en repositorio y mapper")
    void delegaEnRepositorioYMapper() {
        // Arrange
        EstadoProveedor epr = new EstadoProveedor(1, "Activo");
        EstadoProveedorData eprData = new EstadoProveedorData(1, "Activo");
        Proveedor prov = new Proveedor(1, epr, "Norma", "300", "a@a.com", "Dir", "900");
        ProveedorData provData = new ProveedorData(1, eprData, "Norma", "300", "a@a.com", "Dir", "900");
        when(proveedorMapper.toEntity(prov)).thenReturn(provData);
        when(proveedorJpaRepository.save(provData)).thenReturn(provData);
        when(proveedorJpaRepository.findById(1)).thenReturn(Optional.of(provData));
        when(proveedorJpaRepository.findAll()).thenReturn(List.of(provData));
        when(proveedorJpaRepository.findByNit("900")).thenReturn(Optional.of(provData));
        when(proveedorMapper.toDomain(provData)).thenReturn(prov);
        when(productoProveedorJpaRepository.existsById_IdProveedor(1)).thenReturn(true);

        // Act
        Proveedor guardado = gateway.save(prov);
        Optional<Proveedor> porId = gateway.findById(1);
        List<Proveedor> todos = gateway.findAll();
        Optional<Proveedor> porNit = gateway.findByNit("900");
        boolean tieneProductos = gateway.existsProductosByProveedorId(1);

        // Assert
        assertEquals("Norma", guardado.getNombre());
        assertTrue(porId.isPresent());
        assertEquals(1, todos.size());
        assertTrue(porNit.isPresent());
        assertTrue(tieneProductos);
    }
}
