package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiaFestivoTest {

    @Test
    @DisplayName("Debe construir y acceder a las propiedades de DiaFestivo")
    void debeConstruirYAccederPropiedades() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 12, 25);
        DiaFestivo festivo = new DiaFestivo();

        // Act
        festivo.setFecha(fecha);
        festivo.setNombre("Navidad");
        festivo.setFuente(DiaFestivo.FUENTE_API_LIVE);
        DiaFestivo festivoFull = new DiaFestivo(fecha, "Navidad", DiaFestivo.FUENTE_CACHE);

        // Assert
        assertEquals(fecha, festivo.getFecha());
        assertEquals("Navidad", festivo.getNombre());
        assertEquals("API_LIVE", festivo.getFuente());
        assertEquals("CACHE", festivoFull.getFuente());
        assertEquals("CACHE_STALE", DiaFestivo.FUENTE_CACHE_STALE);
    }
}
