package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TasaCambioTest {

    @Test
    @DisplayName("Convierte COP a USD con redondeo HALF_EVEN y asigna valores por defecto")
    void convierteCopAUsdConRedondeoHalfEvenYValidaInvariantes() {
        // Arrange
        TasaCambio tasaCambio = new TasaCambio(new BigDecimal("4000.00"), null, " ");

        // Act
        BigDecimal usdExacto = tasaCambio.convertirAUsd(new BigDecimal("10000.00"));
        BigDecimal usdRedondeado = tasaCambio.convertirAUsd(new BigDecimal("8500.00"));

        // Assert
        assertNotNull(tasaCambio.fechaVigencia());
        assertEquals(TasaCambio.FUENTE_API_LIVE, tasaCambio.fuente());
        assertEquals(new BigDecimal("2.50"), usdExacto);
        assertEquals(new BigDecimal("2.12"), usdRedondeado);
    }

    @Test
    @DisplayName("Rechaza tasa menor o igual a cero y montos negativos o nulos")
    void rechazaTasaOMontosInvalidos() {
        // Arrange
        TasaCambio tasaCambio = new TasaCambio(new BigDecimal("4000.00"), LocalDate.of(2026, 11, 16), TasaCambio.FUENTE_CACHE);

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> tasaCambio.convertirAUsd(null));
        assertThrows(ReglaNegocioException.class, () -> tasaCambio.convertirAUsd(new BigDecimal("-1.00")));
        assertThrows(ReglaNegocioException.class, () -> new TasaCambio(null, LocalDate.now(), "API"));
        assertThrows(ReglaNegocioException.class, () -> new TasaCambio(BigDecimal.ZERO, LocalDate.now(), "API"));
    }
}
