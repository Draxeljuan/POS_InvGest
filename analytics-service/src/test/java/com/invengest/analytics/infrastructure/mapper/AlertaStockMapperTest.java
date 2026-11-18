package com.invengest.analytics.infrastructure.mapper;

import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.AlertaStockData;
import com.invengest.analytics.infrastructure.entry_points.dto.response.AlertaStockResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlertaStockMapperTest {

    private final AlertaStockMapper mapper = Mappers.getMapper(AlertaStockMapper.class);

    @Test
    @DisplayName("Debe convertir entre AlertaStock, AlertaStockData y AlertaStockResponseDTO")
    void mapeoBidireccionalYLista_Exitoso() {
        // Arrange
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 16, 10, 0);
        AlertaStockData entity = new AlertaStockData(1, "PROD-01", "Cuaderno", (short) 2, (short) 5, "Bajo Stock", ahora);

        // Act
        AlertaStock domain = mapper.toDomain(entity);
        AlertaStockData mappedEntity = mapper.toEntity(domain);
        AlertaStockResponseDTO dto = mapper.toResponse(domain);
        List<AlertaStockResponseDTO> dtos = mapper.toResponseList(List.of(domain));

        // Assert
        assertThat(domain.getIdProducto()).isEqualTo("PROD-01");
        assertThat(mappedEntity.getStockMinimo()).isEqualTo((short) 5);
        assertThat(dto.getEstado()).isEqualTo("Bajo Stock");
        assertThat(dtos).hasSize(1);
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toResponseList(null)).isNull();
    }
}
