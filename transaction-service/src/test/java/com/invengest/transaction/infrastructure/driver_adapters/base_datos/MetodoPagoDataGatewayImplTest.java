package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.MetodoPagoData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.MetodoPagoJpaRepository;
import com.invengest.transaction.infrastructure.mapper.MetodoPagoMapper;
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
class MetodoPagoDataGatewayImplTest {

    @Mock
    private MetodoPagoJpaRepository repository;
    @Mock
    private MetodoPagoMapper mapper;

    @InjectMocks
    private MetodoPagoDataGatewayImpl gateway;

    @Test
    @DisplayName("Debe consultar métodos de pago por ID, todos y existencia")
    void debeConsultarMetodosPago() {
        // Arrange
        MetodoPago mpDom = new MetodoPago(1, "Efectivo");
        MetodoPagoData mpEnt = new MetodoPagoData(1, "Efectivo");
        when(repository.findById(1)).thenReturn(Optional.of(mpEnt));
        when(repository.findAll()).thenReturn(List.of(mpEnt));
        when(repository.existsById(1)).thenReturn(true);
        when(mapper.toDomain(mpEnt)).thenReturn(mpDom);

        // Act
        Optional<MetodoPago> porId = gateway.findById(1);
        List<MetodoPago> todos = gateway.findAll();
        boolean existe = gateway.existsById(1);

        // Assert
        assertTrue(porId.isPresent());
        assertEquals(1, todos.size());
        assertTrue(existe);
    }
}
