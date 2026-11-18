package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.CierreCajaData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.CierreCajaDetalleData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.CierreCajaJpaRepository;
import com.invengest.transaction.infrastructure.mapper.CierreCajaMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CierreCajaDataGatewayImplTest {

    @Mock
    private CierreCajaJpaRepository repository;
    @Mock
    private CierreCajaMapper mapper;

    @InjectMocks
    private CierreCajaDataGatewayImpl gateway;

    @Test
    @DisplayName("Debe persistir y consultar cierres de caja vinculando detalles")
    void debePersistirYConsultarCierresCaja() {
        // Arrange
        CierreCaja cierreDom = new CierreCaja();
        CierreCajaData cierreEnt = new CierreCajaData();
        cierreEnt.setDetalles(List.of(new CierreCajaDetalleData()));
        CierreCajaData cierreSinDet = new CierreCajaData();
        cierreSinDet.setDetalles(null);

        when(mapper.toEntity(cierreDom)).thenReturn(cierreEnt, cierreSinDet);
        when(repository.save(any())).thenReturn(cierreEnt);
        when(mapper.toDomain(cierreEnt)).thenReturn(cierreDom);
        when(repository.findById(1)).thenReturn(Optional.of(cierreEnt));
        when(repository.findByFechaCierre(any())).thenReturn(Optional.of(cierreEnt));
        when(repository.existsByFechaCierre(any())).thenReturn(true);
        when(repository.findAll()).thenReturn(List.of(cierreEnt));

        // Act
        CierreCaja guardado = gateway.save(cierreDom);
        CierreCaja guardadoSinDet = gateway.save(cierreDom);
        Optional<CierreCaja> porId = gateway.findById(1);
        Optional<CierreCaja> porFecha = gateway.findByFechaCierre(LocalDate.now());
        boolean existe = gateway.existsByFechaCierre(LocalDate.now());
        List<CierreCaja> todos = gateway.findAll();

        // Assert
        assertEquals(cierreDom, guardado);
        assertEquals(cierreDom, guardadoSinDet);
        assertTrue(porId.isPresent());
        assertTrue(porFecha.isPresent());
        assertTrue(existe);
        assertEquals(1, todos.size());
    }
}
