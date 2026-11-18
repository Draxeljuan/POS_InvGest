package com.invengest.analytics.infrastructure.mapper;

import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.ResumenVentasDiariasData;
import com.invengest.analytics.infrastructure.entry_points.dto.response.ResumenVentaResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResumenVentaMapperTest {

    private final ResumenVentaMapper mapper = Mappers.getMapper(ResumenVentaMapper.class);

    @Test
    @DisplayName("Debe mapear entre ResumenVentasDiarias, ResumenVentasDiariasData y ResumenVentaResponseDTO calculando ventasNetas")
    void mapeoBidireccionalYLista_Exitoso() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        ResumenVentasDiariasData entity = new ResumenVentasDiariasData(
                fecha, new BigDecimal("100000.00"), new BigDecimal("20000.00"), 4
        );

        // Act
        ResumenVentasDiarias domain = mapper.toDomain(entity);
        ResumenVentasDiariasData mappedEntity = mapper.toEntity(domain);
        ResumenVentaResponseDTO dto = mapper.toResponse(domain);
        List<ResumenVentaResponseDTO> dtos = mapper.toResponseList(List.of(domain));

        // Assert
        assertThat(domain.getFecha()).isEqualTo(fecha);
        assertThat(mappedEntity.getTotalVentas()).isEqualByComparingTo("100000.00");
        assertThat(dto.getVentasNetas()).isEqualByComparingTo("80000.00");
        assertThat(dtos).hasSize(1);
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toResponseList(null)).isNull();
    }
}
