package com.invengest.analytics.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FilaControlVentasTest {

    @Test
    @DisplayName("Debe construir y manipular FilaControlVentas para las 6 columnas del reporte RF05")
    void builderYGettersSetters_FuncionanCorrectamente() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 1);

        // Act
        FilaControlVentas fila = FilaControlVentas.builder()
                .fecha(fecha)
                .fechaFormateada("nov-01")
                .ventasDiarias(new BigDecimal("120000.00"))
                .acumuladoVentas(new BigDecimal("120000.00"))
                .salidas("")
                .controlClases("")
                .notas("")
                .build();

        FilaControlVentas otra = new FilaControlVentas();
        otra.setFecha(fecha);
        otra.setFechaFormateada("nov-01");
        otra.setVentasDiarias(BigDecimal.TEN);
        otra.setAcumuladoVentas(BigDecimal.TEN);
        otra.setSalidas("");
        otra.setControlClases("");
        otra.setNotas("");

        // Assert
        assertThat(fila.getFecha()).isEqualTo(fecha);
        assertThat(fila.getFechaFormateada()).isEqualTo("nov-01");
        assertThat(fila.getVentasDiarias()).isEqualByComparingTo("120000.00");
        assertThat(fila.getAcumuladoVentas()).isEqualByComparingTo("120000.00");
        assertThat(fila.getSalidas()).isEmpty();
        assertThat(fila.getControlClases()).isEmpty();
        assertThat(fila.getNotas()).isEmpty();
        assertThat(otra.getVentasDiarias()).isEqualByComparingTo("10");
    }
}
