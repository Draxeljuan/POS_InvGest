package com.invengest.analytics.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReporteMensualVentasTest {

    @Test
    @DisplayName("Debe construir y manipular ReporteMensualVentas con sus filas y total mensual")
    void builderYGettersSetters_FuncionanCorrectamente() {
        // Arrange
        FilaControlVentas fila = FilaControlVentas.builder()
                .fecha(LocalDate.of(2026, 11, 1))
                .fechaFormateada("nov-01")
                .ventasDiarias(new BigDecimal("50000.00"))
                .acumuladoVentas(new BigDecimal("50000.00"))
                .build();

        // Act
        ReporteMensualVentas reporte = ReporteMensualVentas.builder()
                .anio(2026)
                .mes(11)
                .nombreMes("NOVIEMBRE")
                .tituloCabecera("\"VARIEDADES MARELIZA\" NOVIEMBRE 2.026")
                .filas(List.of(fila))
                .totalMes(new BigDecimal("50000.00"))
                .build();

        ReporteMensualVentas vacio = new ReporteMensualVentas();
        vacio.setAnio(2026);
        vacio.setMes(11);
        vacio.setNombreMes("NOVIEMBRE");
        vacio.setTituloCabecera("TITULO");
        vacio.setFilas(List.of());
        vacio.setTotalMes(BigDecimal.ZERO);

        // Assert
        assertThat(reporte.getAnio()).isEqualTo(2026);
        assertThat(reporte.getMes()).isEqualTo(11);
        assertThat(reporte.getNombreMes()).isEqualTo("NOVIEMBRE");
        assertThat(reporte.getTituloCabecera()).isEqualTo("\"VARIEDADES MARELIZA\" NOVIEMBRE 2.026");
        assertThat(reporte.getFilas()).hasSize(1);
        assertThat(reporte.getTotalMes()).isEqualByComparingTo("50000.00");
        assertThat(vacio.getAnio()).isEqualTo(2026);
    }
}
