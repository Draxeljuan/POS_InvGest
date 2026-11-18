package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TasaCambioTest {

    @Test
    @DisplayName("Debe crear TasaCambio con valores por defecto cuando fecha o fuente son nulos y convertir COP a USD")
    void crearYConvertirAUsd_ComportamientoPuro() {
        // Arrange
        TasaCambio trm = new TasaCambio(new BigDecimal("4000.00"), null, " ");
        TasaCambio trmConFuenteNula = new TasaCambio(new BigDecimal("4000.00"), LocalDate.of(2026, 11, 16), null);

        // Act
        BigDecimal montoUsd = trm.convertirAUsd(new BigDecimal("100000.00"));

        // Assert
        assertThat(trm.fechaVigencia()).isNotNull();
        assertThat(trm.fuente()).isEqualTo(TasaCambio.FUENTE_API_LIVE);
        assertThat(trmConFuenteNula.fuente()).isEqualTo(TasaCambio.FUENTE_API_LIVE);
        assertThat(montoUsd).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException cuando el valor TRM o el monto a convertir son inválidos")
    void validaciones_ValoresInvalidos_LanzaExcepcion() {
        // Arrange
        TasaCambio trm = new TasaCambio(new BigDecimal("4000.00"), LocalDate.of(2026, 11, 16), TasaCambio.FUENTE_CACHE);

        // Act & Assert
        assertThatThrownBy(() -> new TasaCambio(null, LocalDate.now(), TasaCambio.FUENTE_CACHE))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> new TasaCambio(BigDecimal.ZERO, LocalDate.now(), TasaCambio.FUENTE_CACHE))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> trm.convertirAUsd(null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> trm.convertirAUsd(new BigDecimal("-1.00")))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
