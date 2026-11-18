package com.invengest.analytics.infrastructure.driver_adapters.base_datos;

import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.AlertaStockData;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.repository.AlertaStockJpaRepository;
import com.invengest.analytics.infrastructure.mapper.AlertaStockMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaStockDataGatewayImplTest {

    @Mock
    private AlertaStockJpaRepository jpaRepository;

    private AlertaStockDataGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        AlertaStockMapper mapper = Mappers.getMapper(AlertaStockMapper.class);
        gateway = new AlertaStockDataGatewayImpl(jpaRepository, mapper);
    }

    @Test
    @DisplayName("Debe listar, filtrar por estado, consultar recientes, buscar por producto, guardar y eliminar alertas")
    void operacionesPersistencia_MapeaCorrectamente() {
        // Arrange
        AlertaStockData entity = new AlertaStockData(
                1, "PROD-01", "Cuaderno", (short) 2, (short) 5, "Bajo Stock", LocalDateTime.now()
        );
        when(jpaRepository.findAllByOrderByFechaAlertaDesc()).thenReturn(List.of(entity));
        when(jpaRepository.findByEstadoOrderByFechaAlertaDesc("Bajo Stock")).thenReturn(List.of(entity));
        when(jpaRepository.findAllByOrderByFechaAlertaDesc(any(Pageable.class))).thenReturn(List.of(entity));
        when(jpaRepository.findFirstByIdProducto("PROD-01")).thenReturn(Optional.of(entity));
        when(jpaRepository.save(any())).thenReturn(entity);

        // Act
        List<AlertaStock> todas = gateway.findAll();
        List<AlertaStock> porEstado = gateway.findByEstado("Bajo Stock");
        List<AlertaStock> recientes = gateway.findRecentAlerts(5);
        Optional<AlertaStock> porProducto = gateway.findByIdProducto("PROD-01");
        AlertaStock guardada = gateway.save(new AlertaStock());
        gateway.deleteByIdProducto("PROD-01");

        // Assert
        assertThat(todas).hasSize(1);
        assertThat(porEstado).hasSize(1);
        assertThat(recientes).hasSize(1);
        assertThat(porProducto).isPresent();
        assertThat(guardada.getStockMinimo()).isEqualTo((short) 5);
        verify(jpaRepository).deleteByIdProducto("PROD-01");
    }
}
