package com.invengest.analytics.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardResumenTest {

    @Test
    @DisplayName("Debe construir y modificar DashboardResumen con todos sus atributos")
    void builderYGettersSetters_FuncionanCorrectamente() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        ResumenVentasDiarias venta = ResumenVentasDiarias.crearVacio(fecha);
        TopProducto top = TopProducto.crearInicial("P1", "Prod", fecha);
        AlertaStock alerta = AlertaStock.builder().idProducto("P1").build();

        // Act
        DashboardResumen resumen = DashboardResumen.builder()
                .fechaConsulta(fecha)
                .ventasDelDia(new BigDecimal("100000.00"))
                .ventasAnuladasDelDia(new BigDecimal("10000.00"))
                .ventasNetasDelDia(new BigDecimal("90000.00"))
                .operacionesDelDia(5)
                .ventasAcumuladasPeriodo(new BigDecimal("450000.00"))
                .historialVentasRecientes(List.of(venta))
                .productosMasVendidos(List.of(top))
                .alertasRecientes(List.of(alerta))
                .build();

        DashboardResumen vacio = new DashboardResumen();
        vacio.setFechaConsulta(fecha);
        vacio.setOperacionesDelDia(2);

        // Assert
        assertThat(resumen.getFechaConsulta()).isEqualTo(fecha);
        assertThat(resumen.getVentasDelDia()).isEqualByComparingTo("100000.00");
        assertThat(resumen.getVentasAnuladasDelDia()).isEqualByComparingTo("10000.00");
        assertThat(resumen.getVentasNetasDelDia()).isEqualByComparingTo("90000.00");
        assertThat(resumen.getOperacionesDelDia()).isEqualTo(5);
        assertThat(resumen.getVentasAcumuladasPeriodo()).isEqualByComparingTo("450000.00");
        assertThat(resumen.getHistorialVentasRecientes()).hasSize(1);
        assertThat(resumen.getProductosMasVendidos()).hasSize(1);
        assertThat(resumen.getAlertasRecientes()).hasSize(1);
        assertThat(vacio.getFechaConsulta()).isEqualTo(fecha);
        assertThat(vacio.getOperacionesDelDia()).isEqualTo(2);
    }
}
