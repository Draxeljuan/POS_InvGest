package com.invengest.analytics.infrastructure.mapper;

import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.TopProductoData;
import com.invengest.analytics.infrastructure.entry_points.dto.response.TopProductoResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TopProductoMapperTest {

    private final TopProductoMapper mapper = Mappers.getMapper(TopProductoMapper.class);

    @Test
    @DisplayName("Debe mapear entre TopProducto, TopProductoData y TopProductoResponseDTO")
    void mapeoBidireccionalYLista_Exitoso() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        TopProductoData entity = new TopProductoData(1, "PROD-01", "Cuaderno", 10, fecha);

        // Act
        TopProducto domain = mapper.toDomain(entity);
        TopProductoData mappedEntity = mapper.toEntity(domain);
        TopProductoResponseDTO dto = mapper.toResponse(domain);
        List<TopProductoResponseDTO> dtos = mapper.toResponseList(List.of(domain));

        // Assert
        assertThat(domain.getIdProducto()).isEqualTo("PROD-01");
        assertThat(mappedEntity.getCantidadVendida()).isEqualTo(10);
        assertThat(dto.getNombreProducto()).isEqualTo("Cuaderno");
        assertThat(dtos).hasSize(1);
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toResponseList(null)).isNull();
    }
}
