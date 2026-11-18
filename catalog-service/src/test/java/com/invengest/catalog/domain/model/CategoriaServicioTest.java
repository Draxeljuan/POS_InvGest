package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoriaServicioTest {

    @Test
    @DisplayName("Valida invariantes y detecta categoría Deshabilitados")
    void validarInvariantesYDetectaDeshabilitados() {
        // Arrange
        CategoriaServicio deshabilitados = new CategoriaServicio(1, "  " + CategoriaServicio.CATEGORIA_DESHABILITADOS + "  ");
        CategoriaServicio impresion = new CategoriaServicio(2, "Impresión");
        CategoriaServicio nula = new CategoriaServicio();

        // Act
        deshabilitados.validarInvariantes();
        impresion.validarInvariantes();
        boolean esDeshab = deshabilitados.esDeshabilitado();
        boolean esImpresionDeshab = impresion.esDeshabilitado();
        boolean esNulaDeshab = nula.esDeshabilitado();

        // Assert
        assertEquals(1, deshabilitados.getIdCategoriaServicio());
        assertEquals(CategoriaServicio.CATEGORIA_DESHABILITADOS, deshabilitados.getNombre());
        assertTrue(esDeshab);
        assertFalse(esImpresionDeshab);
        assertFalse(esNulaDeshab);
    }

    @Test
    @DisplayName("Rechaza categoría de servicio con nombre nulo o vacío")
    void validarInvariantesRechazaNombreVacio() {
        // Arrange
        CategoriaServicio vacia = new CategoriaServicio(1, " ");
        CategoriaServicio nula = new CategoriaServicio(2, null);

        // Act & Assert
        assertThrows(ReglaNegocioException.class, vacia::validarInvariantes);
        assertThrows(ReglaNegocioException.class, nula::validarInvariantes);
    }
}
