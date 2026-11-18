package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopProductoTest {

    @Test
    @DisplayName("Debe crear TopProducto inicial y aplicar valores por defecto cuando nombre o fecha son vacíos")
    void crearInicial_ConValoresValidosYPorDefecto() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);

        // Act
        TopProducto top = TopProducto.crearInicial("PROD-001", "Cuaderno Argollado", fecha);
        TopProducto topSinNombreYFecha = TopProducto.crearInicial("PROD-002", " ", null);
        TopProducto topNombreNulo = TopProducto.crearInicial("PROD-003", null, fecha);

        // Assert
        assertThat(top.getIdProducto()).isEqualTo("PROD-001");
        assertThat(top.getNombreProducto()).isEqualTo("Cuaderno Argollado");
        assertThat(top.getCantidadVendida()).isZero();
        assertThat(top.getFecha()).isEqualTo(fecha);
        assertThat(topSinNombreYFecha.getNombreProducto()).isEqualTo("PROD-002");
        assertThat(topSinNombreYFecha.getFecha()).isNotNull();
        assertThat(topNombreNulo.getNombreProducto()).isEqualTo("PROD-003");
    }

    @Test
    @DisplayName("Debe incrementar cantidad vendida y descontar anulaciones sin bajar de cero")
    void incrementarYDescontar_ComportamientoPuro() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        TopProducto top = TopProducto.crearInicial("PROD-001", "Cuaderno", fecha);
        TopProducto topVacio = new TopProducto();
        TopProducto topAnulacionVacio = new TopProducto();

        // Act
        top.incrementarCantidad(5, "Cuaderno Argollado 100H");
        top.incrementarCantidad(1, " ");
        top.descontarAnulacion(2);
        topVacio.incrementarCantidad(3, null);
        topAnulacionVacio.descontarAnulacion(1);

        // Assert
        assertThat(top.getCantidadVendida()).isEqualTo(4);
        assertThat(top.getNombreProducto()).isEqualTo("Cuaderno Argollado 100H");
        assertThat(topVacio.getCantidadVendida()).isEqualTo(3);
        assertThat(topAnulacionVacio.getCantidadVendida()).isZero();
    }

    @Test
    @DisplayName("Debe hacer clamping en cero cuando la cantidad anulada supera la vendida")
    void descontarAnulacion_SuperaCantidadVendida_ClampingCero() {
        // Arrange
        TopProducto top = TopProducto.crearInicial("PROD-001", "Cuaderno", LocalDate.of(2026, 11, 16));
        top.incrementarCantidad(3, null);

        // Act
        top.descontarAnulacion(10);

        // Assert
        assertThat(top.getCantidadVendida()).isZero();
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException ante idProducto vacío o cantidades menores o iguales a cero")
    void validaciones_DatosInvalidos_LanzaExcepcion() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        TopProducto top = TopProducto.crearInicial("PROD-001", "Cuaderno", fecha);

        // Act & Assert
        assertThatThrownBy(() -> TopProducto.crearInicial(null, "Nombre", fecha))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> TopProducto.crearInicial(" ", "Nombre", fecha))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> top.incrementarCantidad(0, "Nombre"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> top.descontarAnulacion(0))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
