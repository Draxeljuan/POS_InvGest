package com.invengest.transaction.infrastructure.driver_adapters.feign;

import com.invengest.transaction.domain.model.DetalleVenta;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.TipoItem;
import com.invengest.transaction.domain.model.Venta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsFeignAdapterTest {

    @Mock
    private AnalyticsFeignClient analyticsClient;

    @InjectMocks
    private AnalyticsFeignAdapter analyticsAdapter;

    @Test
    @DisplayName("Debe sincronizar venta y anulación en analytics-service aislando fallos remotos")
    void debeSincronizarVentaYAnulacionAislandoFallos() {
        // Arrange
        Venta venta = new Venta(1, 1, LocalDateTime.now(), null,
                new MetodoPago(1, "Efectivo"), new BigDecimal("5000.00"),
                Venta.ESTADO_COMPLETADA, null, null, "Devolución",
                List.of(new DetalleVenta(1, 1, "P1", TipoItem.PRODUCTO, "Cuaderno", new BigDecimal("5000.00"), 1, new BigDecimal("5000.00"))));
        Venta ventaSinFechaNiDetalles = new Venta();
        ventaSinFechaNiDetalles.setDetalles(List.of());

        doThrow(new RuntimeException("Analytics down")).when(analyticsClient).sincronizarVenta(any());
        doThrow(new RuntimeException("Analytics down")).when(analyticsClient).sincronizarAnulacionVenta(any());

        // Act & Assert
        assertDoesNotThrow(() -> analyticsAdapter.sincronizarVenta(null));
        assertDoesNotThrow(() -> analyticsAdapter.sincronizarVenta(venta));
        assertDoesNotThrow(() -> analyticsAdapter.sincronizarVenta(ventaSinFechaNiDetalles));
        assertDoesNotThrow(() -> analyticsAdapter.sincronizarAnulacionVenta(null));
        assertDoesNotThrow(() -> analyticsAdapter.sincronizarAnulacionVenta(venta));
        assertDoesNotThrow(() -> analyticsAdapter.sincronizarAnulacionVenta(ventaSinFechaNiDetalles));
        verify(analyticsClient, times(2)).sincronizarVenta(any());
        verify(analyticsClient, times(2)).sincronizarAnulacionVenta(any());
    }
}
