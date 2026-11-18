package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.model.ProductoProveedor;
import com.invengest.catalog.domain.model.gateway.ProductoProveedorGateway;
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
class ProductoProveedorUseCaseTest {

    @Mock
    private ProductoProveedorGateway productoProveedorGateway;

    private ProductoProveedorUseCase productoProveedorUseCase;

    @BeforeEach
    void setUp() {
        productoProveedorUseCase = new ProductoProveedorUseCase(productoProveedorGateway);
    }

    @Test
    @DisplayName("Asocia, consulta y elimina relación entre producto y proveedor")
    void asociaConsultaYEliminaRelacion() {
        // Arrange
        ProductoProveedor pp = new ProductoProveedor("PROD-0001", 1, new BigDecimal("5000.00"));
        when(productoProveedorGateway.save(any())).thenReturn(pp);
        when(productoProveedorGateway.findByProductoId("PROD-0001")).thenReturn(List.of(pp));
        when(productoProveedorGateway.findByProveedorId(1)).thenReturn(List.of(pp));

        // Act
        ProductoProveedor guardada = productoProveedorUseCase.asociarOActualizar(pp);
        List<ProductoProveedor> porProducto = productoProveedorUseCase.obtenerPorProducto("PROD-0001");
        List<ProductoProveedor> porProveedor = productoProveedorUseCase.obtenerPorProveedor(1);
        productoProveedorUseCase.eliminarAsociacion("PROD-0001", 1);

        // Assert
        assertEquals("PROD-0001", guardada.getIdProducto());
        assertEquals(1, porProducto.size());
        assertEquals(1, porProveedor.size());
        verify(productoProveedorGateway).delete("PROD-0001", 1);
    }
}
