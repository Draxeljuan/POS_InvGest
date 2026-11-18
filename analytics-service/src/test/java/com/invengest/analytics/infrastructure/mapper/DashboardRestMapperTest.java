package com.invengest.analytics.infrastructure.mapper;

import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.DashboardResumen;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.infrastructure.entry_points.dto.response.DashboardResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardRestMapperTest {

    private DashboardRestMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new DashboardRestMapperImpl();
        ReflectionTestUtils.setField(mapper, "resumenVentaMapper", new ResumenVentaMapperImpl());
        ReflectionTestUtils.setField(mapper, "topProductoMapper", new TopProductoMapperImpl());
        ReflectionTestUtils.setField(mapper, "alertaStockMapper", new AlertaStockMapperImpl());
    }

    @Test
    @DisplayName("Debe mapear DashboardResumen a DashboardResponseDTO incluyendo listas anidadas")
    void toResponse_MapeaAtributosYListas() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        DashboardResumen domain = DashboardResumen.builder()
                .fechaConsulta(fecha)
                .ventasDelDia(new BigDecimal("100000.00"))
                .ventasAnuladasDelDia(BigDecimal.ZERO)
                .ventasNetasDelDia(new BigDecimal("100000.00"))
                .operacionesDelDia(3)
                .ventasAcumuladasPeriodo(new BigDecimal("200000.00"))
                .historialVentasRecientes(List.of(ResumenVentasDiarias.crearVacio(fecha)))
                .productosMasVendidos(List.of(TopProducto.crearInicial("P1", "Prod", fecha)))
                .alertasRecientes(List.of(AlertaStock.builder().idProducto("P1").build()))
                .build();

        // Act
        DashboardResponseDTO dto = mapper.toResponse(domain);
        DashboardResponseDTO dtoNulo = mapper.toResponse(null);

        // Assert
        assertThat(dto.getFechaConsulta()).isEqualTo(fecha);
        assertThat(dto.getVentasNetasDelDia()).isEqualByComparingTo("100000.00");
        assertThat(dto.getHistorialVentasRecientes()).hasSize(1);
        assertThat(dto.getProductosMasVendidos()).hasSize(1);
        assertThat(dto.getAlertasRecientes()).hasSize(1);
        assertThat(dtoNulo).isNull();
    }
}
