package com.invengest.gateway.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionAccesoRutaTest {

    @Test
    @DisplayName("Debe crear decisiones para ruta pública y ruta protegida")
    void factoryMethods_DebenCrearInstanciasEsperadas() {
        // Arrange
        TokenClaims claims = new TokenClaims("admin", "Administrador", "1", "invengest-pos-auth");

        // Act
        DecisionAccesoRuta publica = DecisionAccesoRuta.permitirRutaPublica();
        DecisionAccesoRuta protegida = DecisionAccesoRuta.autorizarRutaProtegida(claims);

        // Assert
        assertTrue(publica.rutaPublica());
        assertNull(publica.tokenClaims());
        assertFalse(protegida.rutaPublica());
        assertEquals(claims, protegida.tokenClaims());
    }
}
