package com.invengest.analytics.infrastructure.driver_adapters.base_datos;

import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.TopProductoData;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.repository.TopProductoJpaRepository;
import com.invengest.analytics.infrastructure.mapper.TopProductoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopProductoDataGatewayImplTest {

    @Mock
    private TopProductoJpaRepository jpaRepository;

    private TopProductoDataGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        TopProductoMapper mapper = Mappers.getMapper(TopProductoMapper.class);
        gateway = new TopProductoDataGatewayImpl(jpaRepository, mapper);
    }

    @Test
    @DisplayName("Debe consultar por fecha, rango, top N agrupado, producto+fecha y guardar TopProducto")
    void operacionesPersistencia_MapeaCorrectamente() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        TopProductoData entity = new TopProductoData(1, "PROD-01", "Cuaderno", 10, fecha);

        when(jpaRepository.findByFechaOrderByCantidadVendidaDesc(fecha)).thenReturn(List.of(entity));
        when(jpaRepository.findByFechaBetweenOrderByCantidadVendidaDesc(fecha, fecha)).thenReturn(List.of(entity));
        when(jpaRepository.findTopGroupedByFechaBetween(any(), any(), any())).thenReturn(List.of(entity));
        when(jpaRepository.findFirstByIdProductoAndFecha("PROD-01", fecha)).thenReturn(Optional.of(entity));
        when(jpaRepository.save(any())).thenReturn(entity);

        // Act
        List<TopProducto> porFecha = gateway.findByFecha(fecha);
        List<TopProducto> porRango = gateway.findByFechaBetween(fecha, fecha);
        List<TopProducto> topN = gateway.findTopNByFechaBetween(fecha, fecha, 5);
        Optional<TopProducto> porProductoYFecha = gateway.findByIdProductoAndFecha("PROD-01", fecha);
        TopProducto guardado = gateway.save(TopProducto.crearInicial("PROD-01", "Cuaderno", fecha));

        // Assert
        assertThat(porFecha).hasSize(1);
        assertThat(porRango).hasSize(1);
        assertThat(topN).hasSize(1);
        assertThat(porProductoYFecha).isPresent();
        assertThat(guardado.getCantidadVendida()).isEqualTo(10);
    }
}
