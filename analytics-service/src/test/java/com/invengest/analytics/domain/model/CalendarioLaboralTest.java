package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.gateway.CalendarioFestivosGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalendarioLaboralTest {

    @Test
    @DisplayName("Debe identificar días hábiles, encontrar siguiente día hábil y contar días hábiles en un rango")
    void metodosCalendario_ClasificanDomingosYFestivos() {
        // Arrange
        CalendarioFestivosGateway gatewayStub = anio -> List.of(
                new DiaFestivo(LocalDate.of(anio, 11, 2), "Día de Todos los Santos", DiaFestivo.FUENTE_CACHE),
                new DiaFestivo(LocalDate.of(anio, 11, 16), "Independencia de Cartagena", DiaFestivo.FUENTE_CACHE),
                new DiaFestivo(null, "Fecha Nula", DiaFestivo.FUENTE_CACHE)
        );
        CalendarioLaboral calendario = new CalendarioLaboral(gatewayStub);
        CalendarioLaboral calendarioVacio = new CalendarioLaboral(anio -> null);
        CalendarioLaboral calendarioListaVacia = new CalendarioLaboral(anio -> List.of());

        // Act
        boolean lunesFestivoEsHabil = calendario.esDiaHabil(LocalDate.of(2026, 11, 16));
        boolean domingoEsHabil = calendario.esDiaHabil(LocalDate.of(2026, 11, 15));
        boolean martesSinFestivos = calendarioVacio.esDiaHabil(LocalDate.of(2026, 11, 17));
        boolean miercolesListaVacia = calendarioListaVacia.esDiaHabil(LocalDate.of(2026, 11, 18));
        LocalDate siguienteHabil = calendario.siguienteDiaHabil(LocalDate.of(2026, 11, 14));
        int diasHabilesSemana = calendario.contarDiasHabiles(
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 21)
        );

        // Assert
        assertThat(lunesFestivoEsHabil).isFalse();
        assertThat(domingoEsHabil).isFalse();
        assertThat(martesSinFestivos).isTrue();
        assertThat(miercolesListaVacia).isTrue();
        assertThat(siguienteHabil).isEqualTo(LocalDate.of(2026, 11, 17));
        assertThat(diasHabilesSemana).isEqualTo(5);
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException cuando las fechas son nulas o el rango está invertido")
    void validaciones_FechasInvalidas_LanzaExcepcion() {
        // Arrange
        CalendarioLaboral calendario = new CalendarioLaboral(anio -> List.of());
        LocalDate hoy = LocalDate.of(2026, 11, 16);

        // Act & Assert
        assertThatThrownBy(() -> calendario.esDiaHabil(null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> calendario.siguienteDiaHabil(null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> calendario.contarDiasHabiles(null, hoy))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> calendario.contarDiasHabiles(hoy, null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> calendario.contarDiasHabiles(hoy, hoy.minusDays(1)))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
