package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductoProveedorTest {

    @Test
    @DisplayName("Valida invariantes y normaliza código de producto")
    void validarInvariantesNormalizaCodigoProducto() {
        // Arrange
        ProductoProveedor relacion = new ProductoProveedor(" PROD-0001 ", 1, new BigDecimal("4200.00"));

        // Act
        relacion.validarInvariantes();

        // Assert
        assertEquals("PROD-0001", relacion.getIdProducto());
        assertEquals(1, relacion.getIdProveedor());
        assertEquals(new BigDecimal("4200.00"), relacion.getPrecioCompraProveedor());
    }

    @Test
    @DisplayName("Rechaza datos inválidos en asociación producto-proveedor")
    void validarInvariantesRechazaDatosInvalidos() {
        // Arrange
        ProductoProveedor sinProducto = new ProductoProveedor(" ", 1, BigDecimal.TEN);
        ProductoProveedor sinProveedor = new ProductoProveedor("PROD-0001", null, BigDecimal.TEN);
        ProductoProveedor precioNulo = new ProductoProveedor("PROD-0001", 1, null);
        ProductoProveedor precioNegativo = new ProductoProveedor("PROD-0001", 1, new BigDecimal("-1"));

        // Act & Assert
        assertThrows(ReglaNegocioException.class, sinProducto::validarInvariantes);
        assertThrows(ReglaNegocioException.class, sinProveedor::validarInvariantes);
        assertThrows(ReglaNegocioException.class, precioNulo::validarInvariantes);
        assertThrows(ReglaNegocioException.class, precioNegativo::validarInvariantes);
    }
}
