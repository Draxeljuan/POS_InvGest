package com.invengest.catalog.infrastructure.driver_adapters.feign;

import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.infrastructure.driver_adapters.feign.dto.ContactoUsuarioDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionStockFeignAdapterTest {

    @Mock
    private AuthFeignClient authFeignClient;
    @Mock
    private NotificationFeignClient notificationFeignClient;

    private NotificacionStockFeignAdapter notificacionStockFeignAdapter;

    @BeforeEach
    void setUp() {
        notificacionStockFeignAdapter = new NotificacionStockFeignAdapter(authFeignClient, notificationFeignClient);
    }

    @Test
    @DisplayName("Envía alerta de stock a administradores activos y aísla fallos de red")
    void enviaAlertaStockYAislaFallos() {
        // Arrange
        Producto prod = new Producto("PROD-0001", new Categoria(1, "C", ""), "Cuaderno",
                BigDecimal.TEN, BigDecimal.ONE, LocalDate.now(), 0, 3, "A1", new EstadoProducto(3, "Agotado"));

        when(authFeignClient.obtenerContactosAdministradores())
                .thenReturn(List.of(new ContactoUsuarioDTO(1, "Admin Principal", "admin@invengest.local")));

        // Act & Assert
        assertDoesNotThrow(() -> notificacionStockFeignAdapter.enviarAlertaStock(null));
        assertDoesNotThrow(() -> notificacionStockFeignAdapter.enviarAlertaStock(List.of()));
        assertDoesNotThrow(() -> notificacionStockFeignAdapter.enviarAlertaStock(List.of(prod)));
        verify(notificationFeignClient).enviarAlertaStock(any());

        doThrow(new RuntimeException("Notification caído")).when(notificationFeignClient).enviarAlertaStock(any());
        assertDoesNotThrow(() -> notificacionStockFeignAdapter.enviarAlertaStock(List.of(prod)));

        when(authFeignClient.obtenerContactosAdministradores()).thenReturn(List.of());
        assertDoesNotThrow(() -> notificacionStockFeignAdapter.enviarAlertaStock(List.of(prod)));

        when(authFeignClient.obtenerContactosAdministradores())
                .thenReturn(List.of(new ContactoUsuarioDTO(1, "Sin Correo", " ")));
        assertDoesNotThrow(() -> notificacionStockFeignAdapter.enviarAlertaStock(List.of(prod)));
    }
}
