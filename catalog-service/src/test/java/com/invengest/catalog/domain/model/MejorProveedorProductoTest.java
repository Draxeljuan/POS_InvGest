package com.invengest.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MejorProveedorProductoTest {

    @Test
    @DisplayName("Construye y expone todos los atributos de MejorProveedorProducto")
    void construyeYExponeAtributos() {
        // Arrange
        BigDecimal precioVenta = new BigDecimal("10000.00");
        BigDecimal precioCompra = new BigDecimal("5500.00");

        // Act
        MejorProveedorProducto mejor = new MejorProveedorProducto(
                "PROD-0001",
                "Cuaderno",
                precioVenta,
                3,
                "Editorial Norma",
                "900123",
                "300123",
                "ventas@norma.com",
                precioCompra,
                new BigDecimal("4500.00"),
                new BigDecimal("45.00")
        );

        // Assert
        assertEquals("PROD-0001", mejor.idProducto());
        assertEquals("Cuaderno", mejor.nombreProducto());
        assertEquals(precioVenta, mejor.precioVentaActual());
        assertEquals(3, mejor.idProveedor());
        assertEquals("Editorial Norma", mejor.nombreProveedor());
        assertEquals("900123", mejor.nitProveedor());
        assertEquals("300123", mejor.telefonoProveedor());
        assertEquals("ventas@norma.com", mejor.emailProveedor());
        assertEquals(precioCompra, mejor.precioCompraProveedor());
        assertEquals(new BigDecimal("4500.00"), mejor.margenUnitarioEstimado());
        assertEquals(new BigDecimal("45.00"), mejor.porcentajeMargenEstimado());
    }
}
