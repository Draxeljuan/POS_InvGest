package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReporteEstacionalidadTest {

    @Test
    @DisplayName("Debe calcular métricas de ventas y operaciones separando días hábiles de domingos y festivos")
    void calcular_ClasificaCorrectamenteDiasHabilesYNoHabiles() {
        // Arrange
        List<DiaFestivo> festivos = List.of(
                new DiaFestivo(LocalDate.of(2026, 11, 2), "Todos los Santos", DiaFestivo.FUENTE_CACHE),
                new DiaFestivo(LocalDate.of(2026, 11, 16), "Independencia de Cartagena", DiaFestivo.FUENTE_CACHE),
                new DiaFestivo(null, "Nulo", null)
        );
        List<ResumenVentasDiarias> resumenesNov = List.of(
                ResumenVentasDiarias.builder()
                        .fecha(LocalDate.of(2026, 11, 16))
                        .totalVentas(new BigDecimal("120000.00"))
                        .totalAnulado(BigDecimal.ZERO)
                        .cantidadOperaciones(6)
                        .build(),
                ResumenVentasDiarias.builder()
                        .fecha(LocalDate.of(2026, 11, 17))
                        .totalVentas(new BigDecimal("240000.00"))
                        .totalAnulado(BigDecimal.ZERO)
                        .cantidadOperaciones(12)
                        .build(),
                new ResumenVentasDiarias()
        );

        // Act
        ReporteEstacionalidad estacionalidad = ReporteEstacionalidad.calcular(2026, 11, resumenesNov, festivos);
        ReporteEstacionalidad estacionalidadSinDatos = ReporteEstacionalidad.calcular(2026, 11, null, null);

        // Assert
        assertThat(estacionalidad.getDiasHabiles() + estacionalidad.getDiasNoHabiles()).isEqualTo(30);
        assertThat(estacionalidad.getVentasDiasHabiles()).isEqualByComparingTo("240000.00");
        assertThat(estacionalidad.getVentasDiasNoHabiles()).isEqualByComparingTo("120000.00");
        assertThat(estacionalidad.getOperacionesDiasHabiles()).isEqualTo(12);
        assertThat(estacionalidad.getOperacionesDiasNoHabiles()).isEqualTo(6);
        assertThat(estacionalidad.getFuenteFestivos()).isEqualTo(DiaFestivo.FUENTE_CACHE);
        assertThat(estacionalidadSinDatos.getFuenteFestivos()).isEqualTo(DiaFestivo.FUENTE_API_LIVE);
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException cuando el año o el mes están fuera de rango")
    void calcular_AnioOMesInvalidos_LanzaExcepcion() {
        // Arrange
        List<ResumenVentasDiarias> vacia = List.of();

        // Act & Assert
        assertThatThrownBy(() -> ReporteEstacionalidad.calcular(2026, 0, vacia, List.of()))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ReporteEstacionalidad.calcular(2026, 13, vacia, List.of()))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ReporteEstacionalidad.calcular(1999, 11, vacia, List.of()))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ReporteEstacionalidad.calcular(2101, 11, vacia, List.of()))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
