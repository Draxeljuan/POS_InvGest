package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetodoPagoTest {

    @Test
    @DisplayName("Debe evaluar si el método de pago corresponde a Efectivo")
    void debeEvaluarSiEsEfectivo() {
        // Arrange
        MetodoPago efectivo = new MetodoPago(1, " Efectivo ");
        MetodoPago transferencia = new MetodoPago(2, "Transferencia");
        MetodoPago nulo = new MetodoPago();
        nulo.setIdMetodoPago(3);
        nulo.setNombre(null);

        // Act & Assert
        assertEquals(3, nulo.getIdMetodoPago());
        assertTrue(efectivo.esEfectivo());
        assertFalse(transferencia.esEfectivo());
        assertFalse(nulo.esEfectivo());
    }
}
