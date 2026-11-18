package com.invengest.catalog.infrastructure.driver_adapters.feign;

import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsFeignAdapterTest {

    @Mock
    private AnalyticsFeignClient analyticsFeignClient;

    private AnalyticsFeignAdapter analyticsFeignAdapter;

    @BeforeEach
    void setUp() {
        analyticsFeignAdapter = new AnalyticsFeignAdapter(analyticsFeignClient);
    }

    @Test
    @DisplayName("Envía alerta de stock a analytics-service y aísla fallos en try-catch")
    void enviaAlertaYAislaFallosEnTryCatch() {
        // Arrange
        Producto prod = new Producto("PROD-0001", new Categoria(1, "C", ""), "Cuaderno",
                BigDecimal.TEN, BigDecimal.ONE, LocalDate.now(), 0, 3, "A1", new EstadoProducto(3, "Agotado"));

        // Act & Assert
        assertDoesNotThrow(() -> analyticsFeignAdapter.notificarEstadoStock(null));
        verify(analyticsFeignClient, never()).sincronizarAlertaStock(any());

        assertDoesNotThrow(() -> analyticsFeignAdapter.notificarEstadoStock(prod));
        verify(analyticsFeignClient).sincronizarAlertaStock(any());

        doThrow(new RuntimeException("Analytics caído")).when(analyticsFeignClient).sincronizarAlertaStock(any());
        assertDoesNotThrow(() -> analyticsFeignAdapter.notificarEstadoStock(prod));
    }
}
