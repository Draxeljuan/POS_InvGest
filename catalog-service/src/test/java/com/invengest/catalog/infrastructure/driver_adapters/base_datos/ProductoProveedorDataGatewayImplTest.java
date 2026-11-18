package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.model.ProductoProveedor;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProductoProveedorData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ProductoProveedorDataId;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ProductoProveedorJpaRepository;
import com.invengest.catalog.infrastructure.mapper.ProductoProveedorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoProveedorDataGatewayImplTest {

    @Mock
    private ProductoProveedorJpaRepository productoProveedorJpaRepository;
    @Mock
    private ProductoProveedorMapper productoProveedorMapper;

    private ProductoProveedorDataGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new ProductoProveedorDataGatewayImpl(productoProveedorJpaRepository, productoProveedorMapper);
    }

    @Test
    @DisplayName("Delega operaciones de asociación producto-proveedor en repositorio y mapper")
    void delegaEnRepositorioYMapper() {
        // Arrange
        ProductoProveedor pp = new ProductoProveedor("PROD-0001", 1, BigDecimal.TEN);
        ProductoProveedorData ppData = new ProductoProveedorData(new ProductoProveedorDataId("PROD-0001", 1), BigDecimal.TEN);
        when(productoProveedorMapper.toEntity(pp)).thenReturn(ppData);
        when(productoProveedorJpaRepository.save(ppData)).thenReturn(ppData);
        when(productoProveedorJpaRepository.findById_IdProducto("PROD-0001")).thenReturn(List.of(ppData));
        when(productoProveedorJpaRepository.findById_IdProveedor(1)).thenReturn(List.of(ppData));
        when(productoProveedorMapper.toDomain(ppData)).thenReturn(pp);

        // Act
        ProductoProveedor guardado = gateway.save(pp);
        List<ProductoProveedor> porProducto = gateway.findByProductoId("PROD-0001");
        List<ProductoProveedor> porProveedor = gateway.findByProveedorId(1);
        gateway.delete("PROD-0001", 1);

        // Assert
        assertEquals("PROD-0001", guardado.getIdProducto());
        assertEquals(1, porProducto.size());
        assertEquals(1, porProveedor.size());
        verify(productoProveedorJpaRepository).deleteById(any(ProductoProveedorDataId.class));
    }
}
