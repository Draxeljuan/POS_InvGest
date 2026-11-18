package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TipoDocumentoTest {

    @Test
    @DisplayName("Debe construir y modificar propiedades de TipoDocumento")
    void debeConstruirYModificarPropiedades() {
        // Arrange
        TipoDocumento doc = new TipoDocumento();

        // Act
        doc.setIdTipoDocumento(1);
        doc.setNombre("Cédula de Ciudadanía");
        TipoDocumento docFull = new TipoDocumento(2, "NIT");

        // Assert
        assertEquals(1, doc.getIdTipoDocumento());
        assertEquals("Cédula de Ciudadanía", doc.getNombre());
        assertEquals(2, docFull.getIdTipoDocumento());
        assertEquals("NIT", docFull.getNombre());
    }
}
