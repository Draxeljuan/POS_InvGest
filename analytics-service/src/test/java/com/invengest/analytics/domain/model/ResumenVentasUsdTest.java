package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumenVentasUsdTest {

    @Test
    @DisplayName("Debe calcular resumen en COP y USD desde lista de resúmenes diarios incluyendo anulaciones y nulos")
    void calcularDesdeResumenes_ConversionCorrectaYClamping() {
        // Arrange
        TasaCambio trm = new TasaCambio(new BigDecimal("4000.00"), LocalDate.of(2026, 11, 16), TasaCambio.FUENTE_CACHE);
        List<ResumenVentasDiarias> resumenes = List.of(
                ResumenVentasDiarias.builder()
                        .fecha(LocalDate.of(2026, 11, 15))
                        .totalVentas(new BigDecimal("200000.00"))
                        .totalAnulado(new BigDecimal("40000.00"))
                        .cantidadOperaciones(4)
                        .build(),
                ResumenVentasDiarias.builder()
                        .fecha(LocalDate.of(2026, 11, 16))
                        .totalVentas(new BigDecimal("100000.00"))
                        .totalAnulado(new BigDecimal("20000.00"))
                        .cantidadOperaciones(2)
                        .build(),
                new ResumenVentasDiarias()
        );
        List<ResumenVentasDiarias> resumenesAnuladoMayor = List.of(
                ResumenVentasDiarias.builder()
                        .fecha(LocalDate.of(2026, 11, 16))
                        .totalVentas(new BigDecimal("10000.00"))
                        .totalAnulado(new BigDecimal("50000.00"))
                        .cantidadOperaciones(0)
                        .build()
        );

        // Act
        ResumenVentasUsd resultado = ResumenVentasUsd.calcularDesdeResumenes(
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 16),
                resumenes,
                trm
        );
        ResumenVentasUsd resultadoVacio = ResumenVentasUsd.calcularDesdeResumenes(
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 16),
                null,
                trm
        );
        ResumenVentasUsd resultadoClamping = ResumenVentasUsd.calcularDesdeResumenes(
                LocalDate.of(2026, 11, 16),
                LocalDate.of(2026, 11, 16),
                resumenesAnuladoMayor,
                trm
        );

        // Assert
        assertThat(resultado.getTotalVendidoCop()).isEqualByComparingTo("300000.00");
        assertThat(resultado.getTotalAnuladoCop()).isEqualByComparingTo("60000.00");
        assertThat(resultado.getTotalNetoCop()).isEqualByComparingTo("240000.00");
        assertThat(resultado.getTotalVendidoUsd()).isEqualByComparingTo("75.00");
        assertThat(resultado.getTotalAnuladoUsd()).isEqualByComparingTo("15.00");
        assertThat(resultado.getTotalNetoUsd()).isEqualByComparingTo("60.00");
        assertThat(resultado.getCantidadOperaciones()).isEqualTo(6);
        assertThat(resultado.getTasaCambioTrm()).isEqualByComparingTo("4000.00");
        assertThat(resultado.getFuenteTrm()).isEqualTo("CACHE");
        assertThat(resultadoVacio.getTotalNetoUsd()).isEqualByComparingTo("0.00");
        assertThat(resultadoClamping.getTotalNetoCop()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException cuando las fechas o la TRM son inválidas")
    void calcularDesdeResumenes_ParametrosInvalidos_LanzaExcepcion() {
        // Arrange
        TasaCambio trm = new TasaCambio(new BigDecimal("4000.00"), LocalDate.now(), "CACHE");
        LocalDate hoy = LocalDate.of(2026, 11, 16);

        // Act & Assert
        assertThatThrownBy(() -> ResumenVentasUsd.calcularDesdeResumenes(null, hoy, List.of(), trm))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ResumenVentasUsd.calcularDesdeResumenes(hoy, null, List.of(), trm))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ResumenVentasUsd.calcularDesdeResumenes(hoy, hoy.minusDays(1), List.of(), trm))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ResumenVentasUsd.calcularDesdeResumenes(hoy, hoy, List.of(), null))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
