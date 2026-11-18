package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioTest {

    @Test
    @DisplayName("Valida invariantes y deshabilita servicio asignando categoría Deshabilitados")
    void validarInvariantesYDeshabilitarServicio() {
        // Arrange
        CategoriaServicio catImpresion = new CategoriaServicio(2, "Impresión");
        CategoriaServicio catDeshabilitados = new CategoriaServicio(1, CategoriaServicio.CATEGORIA_DESHABILITADOS);
        Servicio servicio = new Servicio(1, catImpresion, "  Impresión Láser  ", "Color", new BigDecimal("1500.00"));

        // Act
        servicio.validarInvariantes();
        servicio.deshabilitar(catDeshabilitados);

        // Assert
        assertEquals("Impresión Láser", servicio.getNombre());
        assertTrue(servicio.getCategoriaServicio().esDeshabilitado());
    }

    @Test
    @DisplayName("Rechaza nombre vacío, precio negativo o categoría nula al deshabilitar")
    void validarInvariantesRechazaDatosInvalidos() {
        // Arrange
        Servicio sinNombre = new Servicio(1, null, " ", "Desc", BigDecimal.TEN);
        Servicio precioNulo = new Servicio(1, null, "Anillado", "Desc", null);
        Servicio precioNegativo = new Servicio(1, null, "Anillado", "Desc", new BigDecimal("-1"));
        Servicio valido = new Servicio(1, null, "Anillado", "Desc", BigDecimal.TEN);

        // Act & Assert
        assertThrows(ReglaNegocioException.class, sinNombre::validarInvariantes);
        assertThrows(ReglaNegocioException.class, precioNulo::validarInvariantes);
        assertThrows(ReglaNegocioException.class, precioNegativo::validarInvariantes);
        assertThrows(ReglaNegocioException.class, () -> valido.deshabilitar(null));
    }
}
