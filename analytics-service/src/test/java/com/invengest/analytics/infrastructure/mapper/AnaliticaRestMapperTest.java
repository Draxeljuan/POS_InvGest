package com.invengest.analytics.infrastructure.mapper;

import com.invengest.analytics.domain.model.ItemVentaSincronizacion;
import com.invengest.analytics.domain.model.ReporteEstacionalidad;
import com.invengest.analytics.domain.model.ResumenVentasUsd;
import com.invengest.analytics.infrastructure.entry_points.dto.request.ItemVentaSincronizacionRequestDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.response.EstacionalidadResponseDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.response.ResumenVentasUsdResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnaliticaRestMapperTest {

    private final AnaliticaRestMapper mapper = Mappers.getMapper(AnaliticaRestMapper.class);

    @Test
    @DisplayName("Debe mapear ítems de sincronización, ResumenVentasUsd y ReporteEstacionalidad a sus DTOs")
    void mapeosRest_Exitosos() {
        // Arrange
        ItemVentaSincronizacionRequestDTO itemVentaDto = ItemVentaSincronizacionRequestDTO.builder()
                .idProducto("PROD-01")
                .nombreProducto("Cuaderno")
                .cantidadVendida(4)
                .build();
        ItemVentaSincronizacionRequestDTO itemAnulacionDto = ItemVentaSincronizacionRequestDTO.builder()
                .idProducto("PROD-02")
                .cantidadAnulada(2)
                .build();
        ResumenVentasUsd resumenUsd = ResumenVentasUsd.builder()
                .fechaInicio(LocalDate.of(2026, 11, 1))
                .fechaFin(LocalDate.of(2026, 11, 16))
                .totalNetoUsd(new BigDecimal("100.00"))
                .fuenteTrm("CACHE")
                .build();
        ReporteEstacionalidad estacionalidad = ReporteEstacionalidad.builder()
                .anio(2026)
                .mes(11)
                .diasHabiles(24)
                .diasNoHabiles(6)
                .fuenteFestivos("API_LIVE")
                .build();

        // Act
        List<ItemVentaSincronizacion> items = mapper.toDomainItems(List.of(itemVentaDto, itemAnulacionDto));
        ResumenVentasUsdResponseDTO usdDto = mapper.toUsdResponse(resumenUsd);
        EstacionalidadResponseDTO estDto = mapper.toEstacionalidadResponse(estacionalidad);

        // Assert
        assertThat(items).hasSize(2);
        assertThat(items.getFirst().getCantidadVendida()).isEqualTo(4);
        assertThat(items.get(1).getCantidadVendida()).isEqualTo(2);
        assertThat(usdDto.getTotalNetoUsd()).isEqualByComparingTo("100.00");
        assertThat(estDto.getDiasHabiles()).isEqualTo(24);
        assertThat(mapper.toDomainItem(null)).isNull();
        assertThat(mapper.toDomainItems(null)).isNull();
        assertThat(mapper.toUsdResponse(null)).isNull();
        assertThat(mapper.toEstacionalidadResponse(null)).isNull();
    }
}
