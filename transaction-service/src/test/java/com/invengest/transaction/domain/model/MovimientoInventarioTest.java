package com.invengest.transaction.domain.model;

import com.invengest.transaction.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MovimientoInventarioTest {

    @Test
    @DisplayName("Debe validar datos obligatorios y construir movimientos para venta y anulación")
    void debeValidarYConstruirMovimientosParaVentaYAnulacion() {
        // Arrange
        TipoMovimiento salida = new TipoMovimiento(2, TipoMovimiento.MOV_SALIDA_VENTA);
        TipoMovimiento anulacion = new TipoMovimiento(5, TipoMovimiento.MOV_ANULACION_VENTA);
        LocalDateTime fechaFija = LocalDateTime.of(2026, 11, 16, 14, 0);
        String motivoLargo = "X".repeat(300);

        MovimientoInventario manual = new MovimientoInventario(
                1, "PROD-01", 1, new TipoMovimiento(null, TipoMovimiento.MOV_INGRESO_COMPRA), 5, null, "Compra");

        // Act
        manual.validarDatosObligatorios();
        MovimientoInventario movVenta = MovimientoInventario.crearParaVenta("PROD-01", 1, salida, 2, 10, null);
        MovimientoInventario movAnulacion = MovimientoInventario.crearParaAnulacion(
                "PROD-01", 2, anulacion, 2, 10, motivoLargo, fechaFija);
        MovimientoInventario movAnulacionSinMotivo = MovimientoInventario.crearParaAnulacion(
                "PROD-01", 2, anulacion, 2, 10, "  ", null);

        // Assert
        assertNotNull(manual.getFechaMovimiento());
        assertEquals(1, manual.getIdMovimientoInventario());
        assertEquals("Venta #10", movVenta.getObservacion());
        assertEquals(255, movAnulacion.getObservacion().length());
        assertEquals(fechaFija, movAnulacion.getFechaMovimiento());
        assertEquals("Anulación Venta #10", movAnulacionSinMotivo.getObservacion());
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException cuando faltan datos obligatorios en MovimientoInventario")
    void debeLanzarExcepcionCuandoFaltanDatosObligatorios() {
        // Arrange
        TipoMovimiento tipoOk = new TipoMovimiento(1, "Ingreso");
        MovimientoInventario sinProd = new MovimientoInventario(null, " ", 1, tipoOk, 1, LocalDateTime.now(), null);
        MovimientoInventario prodNulo = new MovimientoInventario(null, null, 1, tipoOk, 1, LocalDateTime.now(), null);
        MovimientoInventario sinUsuario = new MovimientoInventario(null, "P1", null, tipoOk, 1, LocalDateTime.now(), null);
        MovimientoInventario cantCero = new MovimientoInventario(null, "P1", 1, tipoOk, 0, LocalDateTime.now(), null);
        MovimientoInventario cantNula = new MovimientoInventario(null, "P1", 1, tipoOk, null, LocalDateTime.now(), null);
        MovimientoInventario sinTipo = new MovimientoInventario(null, "P1", 1, null, 1, LocalDateTime.now(), null);
        MovimientoInventario tipoVacio = new MovimientoInventario(null, "P1", 1, new TipoMovimiento(null, " "), 1, LocalDateTime.now(), null);
        MovimientoInventario tipoNombreNulo = new MovimientoInventario(null, "P1", 1, new TipoMovimiento(null, null), 1, LocalDateTime.now(), null);

        // Act & Assert
        assertThrows(ReglaNegocioException.class, sinProd::validarDatosObligatorios);
        assertThrows(ReglaNegocioException.class, prodNulo::validarDatosObligatorios);
        assertThrows(ReglaNegocioException.class, sinUsuario::validarDatosObligatorios);
        assertThrows(ReglaNegocioException.class, cantCero::validarDatosObligatorios);
        assertThrows(ReglaNegocioException.class, cantNula::validarDatosObligatorios);
        assertThrows(ReglaNegocioException.class, sinTipo::validarDatosObligatorios);
        assertThrows(ReglaNegocioException.class, tipoVacio::validarDatosObligatorios);
        assertThrows(ReglaNegocioException.class, tipoNombreNulo::validarDatosObligatorios);
    }
}
