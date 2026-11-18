package com.invengest.transaction.domain.model;

import com.invengest.transaction.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CierreCajaTest {

    @Test
    @DisplayName("Debe calcular diferencia, evaluar descuadre y registrar calendario laboral")
    void debeCalcularDiferenciaEvaluarDescuadreYRegistrarCalendario() {
        // Arrange
        CierreCaja cierre = new CierreCaja(
                1, LocalDate.of(2026, 11, 16), 1, new BigDecimal("50000.00"), 3,
                new BigDecimal("30000.00"), new BigDecimal("29500.00"), null,
                null, null, "Faltante 500", LocalDateTime.now(), List.of()
        );
        CierreCaja cierreSinEsperado = new CierreCaja();
        cierreSinEsperado.setEfectivoEsperado(null);
        cierreSinEsperado.setEfectivoContado(BigDecimal.ZERO);

        // Act
        BigDecimal diferenciaConDescuadre = cierre.calcularDiferencia();
        cierre.registrarCalendario(false, LocalDate.of(2026, 11, 17));
        BigDecimal diferenciaCero = cierreSinEsperado.calcularDiferencia();

        // Assert
        assertEquals(new BigDecimal("-500.00"), diferenciaConDescuadre);
        assertTrue(cierre.tieneDescuadre());
        assertFalse(cierre.getEsDiaHabil());
        assertEquals(LocalDate.of(2026, 11, 17), cierre.getProximoDiaHabil());
        assertEquals(new BigDecimal("0.00"), diferenciaCero);
        assertFalse(cierreSinEsperado.tieneDescuadre());
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException si efectivo contado es nulo o negativo")
    void debeLanzarExcepcionSiEfectivoContadoEsInvalido() {
        // Arrange
        CierreCaja cierreNulo = new CierreCaja();
        CierreCaja cierreNegativo = new CierreCaja();
        cierreNegativo.setEfectivoContado(new BigDecimal("-10.00"));

        // Act & Assert
        assertFalse(cierreNulo.tieneDescuadre());
        assertThrows(ReglaNegocioException.class, cierreNulo::calcularDiferencia);
        assertThrows(ReglaNegocioException.class, cierreNegativo::calcularDiferencia);
    }
}
