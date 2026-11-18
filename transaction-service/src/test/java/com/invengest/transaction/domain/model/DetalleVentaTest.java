package com.invengest.transaction.domain.model;

import com.invengest.transaction.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetalleVentaTest {

    @Test
    @DisplayName("Debe validar estructura, calcular subtotal e identificar tipo de ítem")
    void debeValidarEstructuraYCalcularSubtotal() {
        // Arrange
        DetalleVenta prod = new DetalleVenta();
        prod.setIdDetalleVenta(1);
        prod.setIdVenta(10);
        prod.setIdItem("PROD-01");
        prod.setTipoItem(TipoItem.PRODUCTO);
        prod.setNombreItem("Resma");
        prod.setPrecioUnitario(new BigDecimal("12500.00"));
        prod.setCantidad(2);

        DetalleVenta serv = new DetalleVenta(2, 10, "SRV-01", TipoItem.SERVICIO, "Copia",
                new BigDecimal("300.00"), 3, null);

        // Act
        prod.validarEstructura();
        BigDecimal subProd = prod.calcularSubtotal();
        serv.validarEstructura();
        BigDecimal subServ = serv.calcularSubtotal();

        // Assert
        assertEquals(1, prod.getIdDetalleVenta());
        assertEquals(10, prod.getIdVenta());
        assertEquals("Resma", prod.getNombreItem());
        assertEquals(new BigDecimal("25000.00"), subProd);
        assertEquals(new BigDecimal("900.00"), subServ);
        assertTrue(prod.esProducto());
        assertFalse(prod.esServicio());
        assertTrue(serv.esServicio());
        assertFalse(serv.esProducto());
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException ante datos inválidos en DetalleVenta")
    void debeLanzarExcepcionCuandoDatosSonInvalidos() {
        // Arrange
        DetalleVenta sinIdItem = new DetalleVenta(null, null, "  ", TipoItem.PRODUCTO, "X", BigDecimal.TEN, 1, null);
        DetalleVenta idItemNulo = new DetalleVenta(null, null, null, TipoItem.PRODUCTO, "X", BigDecimal.TEN, 1, null);
        DetalleVenta sinTipo = new DetalleVenta(null, null, "P1", null, "X", BigDecimal.TEN, 1, null);
        DetalleVenta cantNula = new DetalleVenta(null, null, "P1", TipoItem.PRODUCTO, "X", BigDecimal.TEN, null, null);
        DetalleVenta cantCero = new DetalleVenta(null, null, "P1", TipoItem.PRODUCTO, "X", BigDecimal.TEN, 0, null);
        DetalleVenta precioNulo = new DetalleVenta(null, null, "P1", TipoItem.PRODUCTO, "X", null, 1, null);
        DetalleVenta precioNegativo = new DetalleVenta(null, null, "P1", TipoItem.PRODUCTO, "X", new BigDecimal("-1"), 1, null);

        // Act & Assert
        assertThrows(ReglaNegocioException.class, sinIdItem::validarEstructura);
        assertThrows(ReglaNegocioException.class, idItemNulo::validarEstructura);
        assertThrows(ReglaNegocioException.class, sinTipo::validarEstructura);
        assertThrows(ReglaNegocioException.class, cantNula::validarEstructura);
        assertThrows(ReglaNegocioException.class, cantCero::validarEstructura);
        assertThrows(ReglaNegocioException.class, cantNula::calcularSubtotal);
        assertThrows(ReglaNegocioException.class, cantCero::calcularSubtotal);
        assertThrows(ReglaNegocioException.class, precioNulo::calcularSubtotal);
        assertThrows(ReglaNegocioException.class, precioNegativo::calcularSubtotal);
    }
}
