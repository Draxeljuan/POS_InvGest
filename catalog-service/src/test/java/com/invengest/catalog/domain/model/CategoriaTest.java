package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoriaTest {

    @Test
    @DisplayName("Valida invariantes y normaliza nombre y descripción")
    void validarInvariantesNormalizaNombreYDescripcion() {
        // Arrange
        Categoria categoria = new Categoria(1, "  Arte y Diseño  ", "  Pinturas  ");
        Categoria sinDescripcion = new Categoria(2, "  Escolar  ", null);

        // Act
        categoria.validarInvariantes();
        sinDescripcion.validarInvariantes();

        // Assert
        assertEquals(1, categoria.getIdCategoria());
        assertEquals("Arte y Diseño", categoria.getNombre());
        assertEquals("Pinturas", categoria.getDescripcion());
        assertEquals("Escolar", sinDescripcion.getNombre());
        assertNull(sinDescripcion.getDescripcion());
    }

    @Test
    @DisplayName("Rechaza categoría con nombre nulo o en blanco")
    void validarInvariantesRechazaNombreInvalido() {
        // Arrange
        Categoria nombreBlanco = new Categoria(1, " ", "Desc");
        Categoria nombreNulo = new Categoria();
        nombreNulo.setIdCategoria(2);
        nombreNulo.setNombre(null);

        // Act & Assert
        assertThrows(ReglaNegocioException.class, nombreBlanco::validarInvariantes);
        assertThrows(ReglaNegocioException.class, nombreNulo::validarInvariantes);
    }
}
