package com.invengest.analytics.infrastructure.driver_adapters.base_datos;

import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.ResumenVentasDiariasData;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.repository.ResumenVentasDiariasJpaRepository;
import com.invengest.analytics.infrastructure.mapper.ResumenVentaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumenVentaDataGatewayImplTest {

    @Mock
    private ResumenVentasDiariasJpaRepository jpaRepository;

    private ResumenVentaDataGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        ResumenVentaMapper mapper = Mappers.getMapper(ResumenVentaMapper.class);
        gateway = new ResumenVentaDataGatewayImpl(jpaRepository, mapper);
    }

    @Test
    @DisplayName("Debe consultar por fecha, rango, listar todos y guardar resumen de ventas diarias")
    void operacionesPersistencia_MapeaCorrectamente() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        ResumenVentasDiariasData entity = new ResumenVentasDiariasData(
                fecha, new BigDecimal("80000.00"), BigDecimal.ZERO, 3
        );
        when(jpaRepository.findById(fecha)).thenReturn(Optional.of(entity));
        when(jpaRepository.findByFechaBetweenOrderByFechaAsc(fecha, fecha)).thenReturn(List.of(entity));
        when(jpaRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(entity));
        when(jpaRepository.save(any())).thenReturn(entity);

        // Act
        Optional<ResumenVentasDiarias> porFecha = gateway.findByFecha(fecha);
        List<ResumenVentasDiarias> porRango = gateway.findByFechaBetween(fecha, fecha);
        List<ResumenVentasDiarias> todos = gateway.findAll();
        ResumenVentasDiarias guardado = gateway.save(ResumenVentasDiarias.crearVacio(fecha));

        // Assert
        assertThat(porFecha).isPresent();
        assertThat(porRango).hasSize(1);
        assertThat(todos).hasSize(1);
        assertThat(guardado.getTotalVentas()).isEqualByComparingTo("80000.00");
    }
}
