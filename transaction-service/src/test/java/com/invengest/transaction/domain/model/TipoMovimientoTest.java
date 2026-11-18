package com.invengest.transaction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoMovimientoTest {

    @Test
    @DisplayName("Debe clasificar correctamente movimientos de incremento, decremento y ajustes manuales")
    void debeClasificarMovimientosDeStockYAjustesManuales() {
        // Arrange
        TipoMovimiento ingreso = new TipoMovimiento(1, TipoMovimiento.MOV_INGRESO_COMPRA);
        TipoMovimiento salida = new TipoMovimiento(2, TipoMovimiento.MOV_SALIDA_VENTA);
        TipoMovimiento perdida = new TipoMovimiento(3, TipoMovimiento.MOV_AJUSTE_PERDIDA_DANO);
        TipoMovimiento conteo = new TipoMovimiento(4, TipoMovimiento.MOV_AJUSTE_CONTEO_FISICO);
        TipoMovimiento anulacion = new TipoMovimiento(5, TipoMovimiento.MOV_ANULACION_VENTA);
        TipoMovimiento positivo = new TipoMovimiento();
        positivo.setIdMovimiento(6);
        positivo.setNombre(TipoMovimiento.MOV_AJUSTE_POSITIVO_CONTEO);
        TipoMovimiento nulo = new TipoMovimiento(9, null);

        // Act & Assert
        assertEquals(6, positivo.getIdMovimiento());
        assertTrue(ingreso.esIncrementoStock());
        assertTrue(positivo.esIncrementoStock());
        assertTrue(anulacion.esIncrementoStock());
        assertFalse(salida.esIncrementoStock());
        assertFalse(nulo.esIncrementoStock());

        assertTrue(salida.esDecrementoStock());
        assertTrue(perdida.esDecrementoStock());
        assertTrue(conteo.esDecrementoStock());
        assertFalse(ingreso.esDecrementoStock());
        assertFalse(nulo.esDecrementoStock());

        assertTrue(ingreso.esAjusteManualPermitido());
        assertTrue(perdida.esAjusteManualPermitido());
        assertFalse(salida.esAjusteManualPermitido());
        assertFalse(anulacion.esAjusteManualPermitido());
        assertFalse(nulo.esAjusteManualPermitido());
    }
}
