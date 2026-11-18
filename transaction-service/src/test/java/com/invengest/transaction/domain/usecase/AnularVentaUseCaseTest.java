package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.DetalleVenta;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.model.TipoItem;
import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.AnalyticsGateway;
import com.invengest.transaction.domain.model.gateway.CatalogGateway;
import com.invengest.transaction.domain.model.gateway.MovimientoInventarioGateway;
import com.invengest.transaction.domain.model.gateway.TipoMovimientoGateway;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnularVentaUseCaseTest {

    @Mock
    private VentaGateway ventaGateway;
    @Mock
    private CatalogGateway catalogGateway;
    @Mock
    private MovimientoInventarioGateway movimientoInventarioGateway;
    @Mock
    private TipoMovimientoGateway tipoMovimientoGateway;
    @Mock
    private AnalyticsGateway analyticsGateway;

    @InjectMocks
    private AnularVentaUseCase anularVentaUseCase;

    @Test
    @DisplayName("Debe anular venta, reintegrar stock, registrar Kardex y sincronizar Analytics de forma aislada")
    void debeAnularVentaReintegrarStockYSincronizarAnalytics() {
        // Arrange
        DetalleVenta d1 = new DetalleVenta(1, 50, "PROD-001", TipoItem.PRODUCTO, "Cuaderno",
                new BigDecimal("10000.00"), 2, new BigDecimal("20000.00"));
        Venta ventaExistente = new Venta(
                50, 10, LocalDateTime.of(2026, 11, 16, 9, 0),
                null, new MetodoPago(1, "Efectivo"), new BigDecimal("20000.00"),
                Venta.ESTADO_COMPLETADA, null, null, null, List.of(d1)
        );

        when(ventaGateway.findById(50)).thenReturn(Optional.of(ventaExistente));
        when(tipoMovimientoGateway.findByNombre(TipoMovimiento.MOV_ANULACION_VENTA))
                .thenReturn(Optional.empty());
        when(tipoMovimientoGateway.findById(5))
                .thenReturn(Optional.empty());
        when(ventaGateway.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("Analytics caído")).when(analyticsGateway).sincronizarAnulacionVenta(any());

        // Act
        Venta anulada = anularVentaUseCase.anularVenta(50, 1, "Devolución cliente");

        // Assert
        assertTrue(anulada.estaAnulada());
        assertEquals(1, anulada.getIdUsuarioAnulacion());
        assertEquals("Devolución cliente", anulada.getMotivoAnulacion());
        assertEquals(1, anulada.getDetalles().size());
        verify(catalogGateway, times(1)).reintegrarStockBatch(anyList());
        verify(movimientoInventarioGateway, times(1)).save(any(MovimientoInventario.class));
        verify(analyticsGateway, times(1)).sincronizarAnulacionVenta(anulada);
    }

    @Test
    @DisplayName("Debe lanzar excepciones ante ID nulo, venta inexistente o doble anulación")
    void debeLanzarExcepcionesEnFlujosInvalidos() {
        // Arrange
        Venta ventaYaAnulada = new Venta();
        ventaYaAnulada.setIdVenta(50);
        ventaYaAnulada.setEstado(Venta.ESTADO_ANULADA);
        when(ventaGateway.findById(50)).thenReturn(Optional.of(ventaYaAnulada));
        when(ventaGateway.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> anularVentaUseCase.anularVenta(null, 1, "Motivo"));
        assertThrows(ReglaNegocioException.class, () -> anularVentaUseCase.anularVenta(50, 1, "Segundo intento"));
        assertThrows(RecursoNoEncontradoException.class, () -> anularVentaUseCase.anularVenta(999, 1, "No existe"));
    }
}
