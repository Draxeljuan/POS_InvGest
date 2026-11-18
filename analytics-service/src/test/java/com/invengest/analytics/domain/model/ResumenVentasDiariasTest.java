package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumenVentasDiariasTest {

    @Test
    @DisplayName("Debe crear resumen vacío con fecha explícita o fecha actual cuando es nula")
    void crearVacio_InicializaValoresEnCero() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);

        // Act
        ResumenVentasDiarias resumen = ResumenVentasDiarias.crearVacio(fecha);
        ResumenVentasDiarias resumenFechaNula = ResumenVentasDiarias.crearVacio(null);

        // Assert
        assertThat(resumen.getFecha()).isEqualTo(fecha);
        assertThat(resumen.getTotalVentas()).isEqualByComparingTo("0.00");
        assertThat(resumen.getTotalAnulado()).isEqualByComparingTo("0.00");
        assertThat(resumen.getCantidadOperaciones()).isZero();
        assertThat(resumenFechaNula.getFecha()).isNotNull();
    }

    @Test
    @DisplayName("Debe registrar ventas y anulaciones calculando ventas netas sin bajar de cero")
    void registrarVentaYAnulacion_CalculaNetasConClamping() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        ResumenVentasDiarias resumen = ResumenVentasDiarias.crearVacio(fecha);
        ResumenVentasDiarias resumenDesdeNulos = new ResumenVentasDiarias();
        ResumenVentasDiarias anulacionDesdeNulos = new ResumenVentasDiarias();

        // Act
        resumen.registrarVenta(new BigDecimal("120000.00"));
        resumen.registrarVenta(new BigDecimal("30000.00"));
        resumen.registrarAnulacion(new BigDecimal("50000.00"));
        BigDecimal netas = resumen.calcularVentasNetas();
        resumenDesdeNulos.registrarVenta(new BigDecimal("10000.00"));
        anulacionDesdeNulos.registrarAnulacion(new BigDecimal("5000.00"));

        // Assert
        assertThat(resumen.getTotalVentas()).isEqualByComparingTo("150000.00");
        assertThat(resumen.getTotalAnulado()).isEqualByComparingTo("50000.00");
        assertThat(resumen.getCantidadOperaciones()).isEqualTo(1);
        assertThat(netas).isEqualByComparingTo("100000.00");
        assertThat(resumenDesdeNulos.calcularVentasNetas()).isEqualByComparingTo("10000.00");
        assertThat(anulacionDesdeNulos.calcularVentasNetas()).isEqualByComparingTo("0.00");
        assertThat(new ResumenVentasDiarias().calcularVentasNetas()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Debe hacer clamping a cero cuando las anulaciones superan las ventas o las operaciones")
    void registrarAnulacion_SuperaVentasYOperaciones_ClampingCero() {
        // Arrange
        ResumenVentasDiarias resumen = ResumenVentasDiarias.crearVacio(LocalDate.of(2026, 11, 16));
        resumen.registrarVenta(new BigDecimal("50000.00"));

        // Act
        resumen.registrarAnulacion(new BigDecimal("60000.00"));
        resumen.registrarAnulacion(new BigDecimal("10000.00"));

        // Assert
        assertThat(resumen.getCantidadOperaciones()).isZero();
        assertThat(resumen.calcularVentasNetas()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException cuando el monto de venta o anulación es nulo o negativo")
    void registrarVentaYAnulacion_MontosInvalidos_LanzaExcepcion() {
        // Arrange
        ResumenVentasDiarias resumen = ResumenVentasDiarias.crearVacio(LocalDate.of(2026, 11, 16));

        // Act & Assert
        assertThatThrownBy(() -> resumen.registrarVenta(null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> resumen.registrarVenta(new BigDecimal("-1.00")))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> resumen.registrarAnulacion(null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> resumen.registrarAnulacion(new BigDecimal("-1.00")))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
