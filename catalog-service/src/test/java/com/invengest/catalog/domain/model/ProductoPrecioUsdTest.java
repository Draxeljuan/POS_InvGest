package com.invengest.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductoPrecioUsdTest {

    @Test
    @DisplayName("Construye y expone todos los atributos de ProductoPrecioUsd")
    void construyeYExponeAtributos() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);

        // Act
        ProductoPrecioUsd dto = new ProductoPrecioUsd(
                "PROD-0001",
                "Cuaderno",
                "Escolar",
                new BigDecimal("8000.00"),
                new BigDecimal("2.00"),
                new BigDecimal("4000.00"),
                fecha,
                TasaCambio.FUENTE_CACHE,
                10,
                EstadoProducto.ESTADO_ACTIVO
        );

        // Assert
        assertEquals("PROD-0001", dto.idProducto());
        assertEquals("Cuaderno", dto.nombre());
        assertEquals("Escolar", dto.categoria());
        assertEquals(new BigDecimal("8000.00"), dto.precioVentaCop());
        assertEquals(new BigDecimal("2.00"), dto.precioVentaUsd());
        assertEquals(new BigDecimal("4000.00"), dto.trmVigente());
        assertEquals(fecha, dto.fechaTrm());
        assertEquals(TasaCambio.FUENTE_CACHE, dto.fuenteTrm());
        assertEquals(10, dto.stock());
        assertEquals(EstadoProducto.ESTADO_ACTIVO, dto.estado());
    }
}
