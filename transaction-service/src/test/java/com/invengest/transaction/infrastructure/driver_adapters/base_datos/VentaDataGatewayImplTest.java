package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.DetalleVentaData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.VentaData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.VentaJpaRepository;
import com.invengest.transaction.infrastructure.mapper.VentaMapper;
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
class VentaDataGatewayImplTest {

    @Mock
    private VentaJpaRepository ventaJpaRepository;
    @Mock
    private VentaMapper ventaMapper;

    @InjectMocks
    private VentaDataGatewayImpl ventaDataGateway;

    @Test
    @DisplayName("Debe persistir y consultar ventas vinculando detalles bidireccionales")
    void debePersistirYConsultarVentas() {
        // Arrange
        Venta ventaDom = new Venta();
        ventaDom.setIdVenta(1);
        VentaData ventaEnt = new VentaData();
        ventaEnt.setIdVenta(1);
        ventaEnt.setDetalles(List.of(new DetalleVentaData()));

        VentaData ventaSinDetalles = new VentaData();
        ventaSinDetalles.setDetalles(null);

        when(ventaMapper.toEntity(ventaDom)).thenReturn(ventaEnt, ventaSinDetalles);
        when(ventaJpaRepository.save(any())).thenReturn(ventaEnt);
        when(ventaMapper.toDomain(ventaEnt)).thenReturn(ventaDom);
        when(ventaJpaRepository.findByIdWithDetails(1)).thenReturn(Optional.empty());
        when(ventaJpaRepository.findById(1)).thenReturn(Optional.of(ventaEnt));
        when(ventaJpaRepository.findAll()).thenReturn(List.of(ventaEnt));
        when(ventaJpaRepository.findByFechaBetween(any(), any())).thenReturn(List.of(ventaEnt));
        when(ventaJpaRepository.findByIdUsuario(1)).thenReturn(List.of(ventaEnt));

        // Act
        Venta guardada = ventaDataGateway.save(ventaDom);
        Venta guardadaSinDet = ventaDataGateway.save(ventaDom);
        Optional<Venta> porId = ventaDataGateway.findById(1);
        List<Venta> todas = ventaDataGateway.findAll();
        List<Venta> porFecha = ventaDataGateway.findByFechaBetween(LocalDateTime.now(), LocalDateTime.now());
        List<Venta> porUsuario = ventaDataGateway.findByIdUsuario(1);

        // Assert
        assertEquals(1, guardada.getIdVenta());
        assertEquals(1, guardadaSinDet.getIdVenta());
        assertTrue(porId.isPresent());
        assertEquals(1, todas.size());
        assertEquals(1, porFecha.size());
        assertEquals(1, porUsuario.size());
    }
}
