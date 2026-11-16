package com.invengest.catalog;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import com.invengest.catalog.domain.exception.StockInsuficienteException;
import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.ProductoProveedor;
import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.domain.model.Servicio;
import com.invengest.catalog.domain.model.SolicitudItemStock;
import com.invengest.catalog.domain.model.TasaCambio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainModelsTest {

    @Test
    void productoDescontarStockYMarcarAgotadoCuandoLlegaACero() {
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
    void productoDescontarStockLanzaExcepcionCuandoStockInsuficienteODescontinuado() {
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

        // Act & Assert
        assertFalse(producto.tieneStockSuficiente(0));
        assertTrue(producto.tieneStockSuficiente(2));
        assertFalse(producto.tieneStockSuficiente(3));
        assertThrows(StockInsuficienteException.class, () -> producto.descontarStock(3, agotado));
        assertThrows(ReglaNegocioException.class, () -> producto.descontarStock(0, agotado));
        assertThrows(ReglaNegocioException.class, () -> producto.descontinuar(null));

        producto.descontinuar(descontinuado);
        assertFalse(producto.tieneStockSuficiente(1));
        assertThrows(ReglaNegocioException.class, () -> producto.descontarStock(1, agotado));
    }

    @Test
    void productoReintegrarStockReactivaProductoAgotadoYCaculaMargenes() {
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
    void productoValidarInvariantesRechazaDatosInvalidos() {
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
    void tasaCambioConvierteCopAUsdConRedondeoHalfEvenYValidaInvariantes() {
        // Arrange
        TasaCambio tasaCambio = new TasaCambio(
                new BigDecimal("4000.00"),
                null,
                " "
        );

        // Act
        BigDecimal usdExacto = tasaCambio.convertirAUsd(new BigDecimal("10000.00"));
        BigDecimal usdRedondeado = tasaCambio.convertirAUsd(new BigDecimal("8500.00"));

        // Assert
        assertNotNull(tasaCambio.fechaVigencia());
        assertEquals(TasaCambio.FUENTE_API_LIVE, tasaCambio.fuente());
        assertEquals(new BigDecimal("2.50"), usdExacto);
        assertEquals(new BigDecimal("2.12"), usdRedondeado);
        assertThrows(ReglaNegocioException.class, () -> tasaCambio.convertirAUsd(null));
        assertThrows(ReglaNegocioException.class, () -> tasaCambio.convertirAUsd(new BigDecimal("-1.00")));
        assertThrows(ReglaNegocioException.class, () -> new TasaCambio(null, LocalDate.now(), "API"));
        assertThrows(ReglaNegocioException.class, () -> new TasaCambio(BigDecimal.ZERO, LocalDate.now(), "API"));
    }

    @Test
    void entidadesSecundariasValidanInvariantesYComportamientos() {
        // Arrange
        Categoria categoria = new Categoria(1, "  Arte y Diseño  ", "  Pinturas  ");
        CategoriaServicio catServ = new CategoriaServicio(1, CategoriaServicio.CATEGORIA_DESHABILITADOS);
        EstadoProducto epNulo = new EstadoProducto(1, null);
        EstadoProveedor estProvActivo = new EstadoProveedor(1, EstadoProveedor.ESTADO_ACTIVO);
        EstadoProveedor estProvInactivo = new EstadoProveedor(2, EstadoProveedor.ESTADO_INACTIVO);
        EstadoProveedor estProvNulo = new EstadoProveedor(3, null);
        Proveedor proveedor = new Proveedor(1, estProvActivo, "  Distribuidora Norma  ", "300123", "ventas@norma.com", "Calle 10", " 900123-1 ");
        Servicio servicio = new Servicio(1, new CategoriaServicio(2, "Impresión"), "  Impresión Láser  ", "Color", new BigDecimal("1500.00"));
        ProductoProveedor prodProv = new ProductoProveedor(" PROD-0001 ", 1, new BigDecimal("4200.00"));
        SolicitudItemStock itemStock = new SolicitudItemStock(" PROD-0001 ", 3);

        // Act
        categoria.validarInvariantes();
        catServ.validarInvariantes();
        proveedor.validarInvariantes();
        servicio.validarInvariantes();
        prodProv.validarInvariantes();
        boolean provEstabaActivo = proveedor.estaActivo();
        proveedor.desactivar(estProvInactivo);
        servicio.deshabilitar(catServ);

        // Assert
        assertEquals("Arte y Diseño", categoria.getNombre());
        assertTrue(catServ.esDeshabilitado());
        assertFalse(epNulo.esActivo());
        assertFalse(epNulo.esAgotado());
        assertFalse(epNulo.esDescontinuado());
        assertFalse(estProvNulo.esActivo());
        assertFalse(estProvNulo.esInactivo());
        assertTrue(provEstabaActivo);
        assertTrue(proveedor.getEstado().esInactivo());
        assertTrue(servicio.getCategoriaServicio().esDeshabilitado());
        assertEquals("PROD-0001", prodProv.getIdProducto());
        assertEquals("PROD-0001", itemStock.idProducto());

        assertThrows(ReglaNegocioException.class, () -> new Categoria(1, " ", null).validarInvariantes());
        assertThrows(ReglaNegocioException.class, () -> new CategoriaServicio(1, "").validarInvariantes());
        assertThrows(ReglaNegocioException.class, () -> new Proveedor(1, null, " ", null, null, null, null).validarInvariantes());
        assertThrows(ReglaNegocioException.class, () -> proveedor.desactivar(null));
        assertThrows(ReglaNegocioException.class, () -> new Servicio(1, null, " ", null, BigDecimal.TEN).validarInvariantes());
        assertThrows(ReglaNegocioException.class, () -> new Servicio(1, null, "S", null, new BigDecimal("-1")).validarInvariantes());
        assertThrows(ReglaNegocioException.class, () -> servicio.deshabilitar(null));
        assertThrows(ReglaNegocioException.class, () -> new ProductoProveedor("", 1, BigDecimal.TEN).validarInvariantes());
        assertThrows(ReglaNegocioException.class, () -> new ProductoProveedor("P", null, BigDecimal.TEN).validarInvariantes());
        assertThrows(ReglaNegocioException.class, () -> new ProductoProveedor("P", 1, new BigDecimal("-1")).validarInvariantes());
        assertThrows(ReglaNegocioException.class, () -> new SolicitudItemStock(null, 1));
        assertThrows(ReglaNegocioException.class, () -> new SolicitudItemStock("PROD-1", null));
    }
}
