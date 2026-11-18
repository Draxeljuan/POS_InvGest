package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertaStockTest {

    @Test
    @DisplayName("Debe evaluar si requiere alerta según stockActual y stockMinimo")
    void requiereAlerta_EvaluaUmbralRealYDefecto() {
        // Arrange
        Short stockBajo = 4;
        Short stockAgotado = 0;
        Short stockNormal = 6;
        Short stockMinimo = 5;

        // Act
        boolean requiereBajo = AlertaStock.requiereAlerta(stockBajo, stockMinimo);
        boolean requiereAgotado = AlertaStock.requiereAlerta(stockAgotado, (short) 0);
        boolean requiereNormal = AlertaStock.requiereAlerta(stockNormal, stockMinimo);
        boolean requiereDefecto = AlertaStock.requiereAlerta((short) 9, null);

        // Assert
        assertThat(requiereBajo).isTrue();
        assertThat(requiereAgotado).isTrue();
        assertThat(requiereNormal).isFalse();
        assertThat(requiereDefecto).isTrue();
    }

    @Test
    @DisplayName("Debe actualizar datos de alerta y normalizar estados de filtro correctamente")
    void actualizarYNormalizar_ComportamientoPuro() {
        // Arrange
        AlertaStock alertaAgotado = AlertaStock.builder().idProducto("PROD-010").build();
        AlertaStock alertaBajo = AlertaStock.builder().idProducto("PROD-011").build();

        // Act
        alertaAgotado.actualizarDatosAlerta("Lapicero Negro", (short) 0, (short) 5);
        alertaBajo.actualizarDatosAlerta("Cuaderno", (short) 3, (short) -2);
        String filtroBajo1 = AlertaStock.normalizarEstadoFiltro("BAJO_STOCK");
        String filtroBajo2 = AlertaStock.normalizarEstadoFiltro("Bajo Stock");
        String filtroAgotado1 = AlertaStock.normalizarEstadoFiltro("Agotado");
        String filtroAgotado2 = AlertaStock.normalizarEstadoFiltro("AGOTADO");

        // Assert
        assertThat(alertaAgotado.getEstado()).isEqualTo(AlertaStock.ESTADO_AGOTADO);
        assertThat(alertaAgotado.getStockMinimo()).isEqualTo((short) 5);
        assertThat(alertaAgotado.getFechaAlerta()).isNotNull();
        assertThat(alertaBajo.getEstado()).isEqualTo(AlertaStock.ESTADO_BAJO_STOCK);
        assertThat(alertaBajo.getStockMinimo()).isZero();
        assertThat(filtroBajo1).isEqualTo(AlertaStock.ESTADO_BAJO_STOCK);
        assertThat(filtroBajo2).isEqualTo(AlertaStock.ESTADO_BAJO_STOCK);
        assertThat(filtroAgotado1).isEqualTo(AlertaStock.ESTADO_AGOTADO);
        assertThat(filtroAgotado2).isEqualTo(AlertaStock.ESTADO_AGOTADO);
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException cuando los datos de alerta o estado de filtro son inválidos")
    void validaciones_DatosInvalidos_LanzaExcepcion() {
        // Arrange
        AlertaStock alerta = AlertaStock.builder().idProducto("PROD-010").build();
        AlertaStock alertaIdBlanco = AlertaStock.builder().idProducto(" ").build();

        // Act & Assert
        assertThatThrownBy(() -> AlertaStock.requiereAlerta(null, (short) 5))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> AlertaStock.evaluarEstado(null, (short) 5))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> AlertaStock.normalizarEstadoFiltro(null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> AlertaStock.normalizarEstadoFiltro(" "))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> AlertaStock.normalizarEstadoFiltro("INVALIDO"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> new AlertaStock().actualizarDatosAlerta("Prod", (short) 1, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> alertaIdBlanco.actualizarDatosAlerta("Prod", (short) 1, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> alerta.actualizarDatosAlerta(null, (short) 1, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> alerta.actualizarDatosAlerta(" ", (short) 1, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> alerta.actualizarDatosAlerta("Prod", null, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
