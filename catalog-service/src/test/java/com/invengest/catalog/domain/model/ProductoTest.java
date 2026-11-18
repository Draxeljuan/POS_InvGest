package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import com.invengest.catalog.domain.exception.StockInsuficienteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoTest {

    @Test
    @DisplayName("Descuenta stock y marca agotado cuando llega a cero")
    void descontarStockYMarcarAgotadoCuandoLlegaACero() {
        // Arrange
        EstadoProducto activo = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        EstadoProducto agotado = new EstadoProducto(3, EstadoProducto.ESTADO_AGOTADO);
        Producto producto = new Producto(
                "PROD-0001",
                new Categoria(1, "Cuadernos", "Escolares"),
                "Cuaderno Cosido 100 Hojas",
                new BigDecimal("8500.00"),
                new BigDecimal("5000.00"),
                LocalDate.of(2026, 11, 1),
                5,
                3,
                "Estante A1",
                activo
        );

        // Act
        producto.descontarStock(2, agotado);
        String etiquetaBajoStock = producto.obtenerEtiquetaAlertaStock();
        producto.descontarStock(3, agotado);

        // Assert
        assertEquals("Bajo Stock", etiquetaBajoStock);
        assertEquals(0, producto.getStock());
        assertTrue(producto.estaAgotado());
        assertTrue(producto.estaEnStockBajo());
        assertEquals(EstadoProducto.ESTADO_AGOTADO, producto.obtenerEtiquetaAlertaStock());
        assertEquals(EstadoProducto.ESTADO_AGOTADO, producto.getEstado().getNombre());
    }

    @Test
    @DisplayName("Descontar stock lanza excepción cuando stock insuficiente o descontinuado")
    void descontarStockLanzaExcepcionCuandoStockInsuficienteODescontinuado() {
        // Arrange
        EstadoProducto activo = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        EstadoProducto descontinuado = new EstadoProducto(2, EstadoProducto.ESTADO_DESCONTINUADO);
        EstadoProducto agotado = new EstadoProducto(3, EstadoProducto.ESTADO_AGOTADO);
        Producto producto = new Producto(
                "PROD-0002",
                new Categoria(1, "Papelería", "General"),
                "Resma Carta",
                new BigDecimal("18000.00"),
                new BigDecimal("12000.00"),
                LocalDate.now(),
                2,
                5,
                "Bodega",
                activo
        );

        // Act
        boolean suficienteCero = producto.tieneStockSuficiente(0);
        boolean suficienteDos = producto.tieneStockSuficiente(2);
        boolean suficienteTres = producto.tieneStockSuficiente(3);

        // Assert
        assertFalse(suficienteCero);
        assertTrue(suficienteDos);
        assertFalse(suficienteTres);
        assertThrows(StockInsuficienteException.class, () -> producto.descontarStock(3, agotado));
        assertThrows(ReglaNegocioException.class, () -> producto.descontarStock(0, agotado));
        assertThrows(ReglaNegocioException.class, () -> producto.descontinuar(null));

        producto.descontinuar(descontinuado);
        assertFalse(producto.tieneStockSuficiente(1));
        assertThrows(ReglaNegocioException.class, () -> producto.descontarStock(1, agotado));
    }

    @Test
    @DisplayName("Reintegrar stock reactiva producto agotado y calcula márgenes")
    void reintegrarStockReactivaProductoAgotadoYCalculaMargenes() {
        // Arrange
        EstadoProducto agotado = new EstadoProducto(3, EstadoProducto.ESTADO_AGOTADO);
        EstadoProducto activo = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        Producto producto = new Producto(
                "PROD-0003",
                new Categoria(1, "Escritura", "Bolígrafos"),
                "Caja Esferos Negros x12",
                new BigDecimal("10000.00"),
                new BigDecimal("6000.00"),
                null,
                0,
                2,
                "Vitrina 1",
                agotado
        );

        // Act
        producto.validarInvariantes();
        producto.reintegrarStock(10, activo);
        BigDecimal margenUnitario = producto.calcularMargenUnitario(new BigDecimal("6500.00"));
        BigDecimal porcentajeMargen = producto.calcularPorcentajeMargen(new BigDecimal("6500.00"));
        BigDecimal margenNulo = producto.calcularMargenUnitario(null);
        BigDecimal porcentajeNulo = producto.calcularPorcentajeMargen(null);

        // Assert
        assertNotNull(producto.getFechaIngreso());
        assertEquals(10, producto.getStock());
        assertTrue(producto.getEstado().esActivo());
        assertFalse(producto.estaEnStockBajo());
        assertEquals(new BigDecimal("3500.00"), margenUnitario);
        assertEquals(new BigDecimal("35.00"), porcentajeMargen);
        assertEquals(new BigDecimal("0.00"), margenNulo);
        assertEquals(new BigDecimal("0.00"), porcentajeNulo);
        assertThrows(ReglaNegocioException.class, () -> producto.reintegrarStock(0, activo));
    }

    @Test
    @DisplayName("Validar invariantes rechaza datos inválidos")
    void validarInvariantesRechazaDatosInvalidos() {
        // Arrange
        Producto sinNombre = new Producto(null, null, " ", BigDecimal.TEN, BigDecimal.ONE, null, 1, 1, null, null);
        Producto precioNulo = new Producto(null, null, "Lápiz", null, BigDecimal.ONE, null, 1, 1, null, null);
        Producto precioNegativo = new Producto(null, null, "Lápiz", new BigDecimal("-1"), BigDecimal.ONE, null, 1, 1, null, null);
        Producto costoNulo = new Producto(null, null, "Lápiz", BigDecimal.TEN, null, null, 1, 1, null, null);
        Producto costoNegativo = new Producto(null, null, "Lápiz", BigDecimal.TEN, new BigDecimal("-1"), null, 1, 1, null, null);
        Producto precioMenorCosto = new Producto(null, null, "Lápiz", new BigDecimal("500"), new BigDecimal("800"), null, 1, 1, null, null);
        Producto stockNulo = new Producto(null, null, "Lápiz", BigDecimal.TEN, BigDecimal.ONE, null, null, 1, null, null);
        Producto stockNegativo = new Producto(null, null, "Lápiz", BigDecimal.TEN, BigDecimal.ONE, null, -1, 1, null, null);
        Producto stockMinimoNulo = new Producto(null, null, "Lápiz", BigDecimal.TEN, BigDecimal.ONE, null, 1, null, null, null);
        Producto stockMinimoNegativo = new Producto(null, null, "Lápiz", BigDecimal.TEN, BigDecimal.ONE, null, 1, -1, null, null);

        // Act & Assert
        assertThrows(ReglaNegocioException.class, sinNombre::validarInvariantes);
        assertThrows(ReglaNegocioException.class, precioNulo::validarInvariantes);
        assertThrows(ReglaNegocioException.class, precioNegativo::validarInvariantes);
        assertThrows(ReglaNegocioException.class, costoNulo::validarInvariantes);
        assertThrows(ReglaNegocioException.class, costoNegativo::validarInvariantes);
        assertThrows(ReglaNegocioException.class, precioMenorCosto::validarInvariantes);
        assertThrows(ReglaNegocioException.class, stockNulo::validarInvariantes);
        assertThrows(ReglaNegocioException.class, stockNegativo::validarInvariantes);
        assertThrows(ReglaNegocioException.class, stockMinimoNulo::validarInvariantes);
        assertThrows(ReglaNegocioException.class, stockMinimoNegativo::validarInvariantes);
    }

    @Test
    @DisplayName("Maneja campos opcionales y estados nulos en cálculos de stock y margen")
    void manejaCamposOpcionalesYEstadosNulosEnStockYMargen() {
        // Arrange
        Producto producto = new Producto();
        producto.setNombre("Producto Prueba");
        producto.setPrecioVenta(BigDecimal.ZERO);
        producto.setCostoCompra(BigDecimal.ZERO);
        producto.setStock(null);
        producto.setStockMinimo(null);
        producto.setEstado(null);

        // Act
        boolean suficiente = producto.tieneStockSuficiente(1);
        boolean stockBajo = producto.estaEnStockBajo();
        boolean agotado = producto.estaAgotado();
        BigDecimal porcentajeCero = producto.calcularPorcentajeMargen(BigDecimal.ONE);
        producto.reintegrarStock(3, new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO));

        // Assert
        assertFalse(suficiente);
        assertTrue(stockBajo);
        assertTrue(agotado);
        assertEquals(new BigDecimal("0.00"), porcentajeCero);
        assertEquals(3, producto.getStock());
        assertTrue(producto.getEstado().esActivo());
    }
}
