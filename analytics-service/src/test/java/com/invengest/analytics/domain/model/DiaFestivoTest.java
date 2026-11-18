package com.invengest.analytics.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DiaFestivoTest {

    @Test
    @DisplayName("Debe crear y manipular DiaFestivo con sus constantes de fuente")
    void constructoresYGettersSetters_FuncionanCorrectamente() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);

        // Act
        DiaFestivo festivo = new DiaFestivo(fecha, "Independencia de Cartagena", DiaFestivo.FUENTE_API_LIVE);
        DiaFestivo vacio = new DiaFestivo();
        vacio.setFecha(fecha);
        vacio.setNombre("Otro Festivo");
        vacio.setFuente(DiaFestivo.FUENTE_CACHE_STALE);

        // Assert
        assertThat(festivo.getFecha()).isEqualTo(fecha);
        assertThat(festivo.getNombre()).isEqualTo("Independencia de Cartagena");
        assertThat(festivo.getFuente()).isEqualTo("API_LIVE");
        assertThat(vacio.getFecha()).isEqualTo(fecha);
        assertThat(vacio.getNombre()).isEqualTo("Otro Festivo");
        assertThat(vacio.getFuente()).isEqualTo("CACHE_STALE");
        assertThat(DiaFestivo.FUENTE_CACHE).isEqualTo("CACHE");
    }
}
