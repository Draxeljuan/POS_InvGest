package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServicioCatalogoTest {

    @Test
    @DisplayName("Debe construir y acceder a las propiedades de ServicioCatalogo")
    void debeConstruirYAccederPropiedades() {
        // Arrange
        ServicioCatalogo serv = new ServicioCatalogo();

        // Act
        serv.setIdServicio(1);
        serv.setNombre("Fotocopia");
        serv.setDescripcion("Blanco y negro");
        serv.setPrecioSugerido(new BigDecimal("300.00"));
        ServicioCatalogo servFull = new ServicioCatalogo(2, "Anillado", "Plástico", new BigDecimal("4000.00"));

        // Assert
        assertEquals(1, serv.getIdServicio());
        assertEquals("Fotocopia", serv.getNombre());
        assertEquals("Blanco y negro", serv.getDescripcion());
        assertEquals(new BigDecimal("300.00"), serv.getPrecioSugerido());
        assertEquals(2, servFull.getIdServicio());
    }
}
