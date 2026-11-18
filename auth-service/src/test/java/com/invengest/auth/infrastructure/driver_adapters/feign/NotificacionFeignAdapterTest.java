package com.invengest.auth.infrastructure.driver_adapters.feign;

import com.invengest.auth.domain.model.EventoSeguridadCuenta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificacionFeignAdapterTest {

    @Mock
    private NotificationFeignClient notificationFeignClient;

    @InjectMocks
    private NotificacionFeignAdapter adapter;

    @Test
    @DisplayName("Debe enviar notificaciones de bienvenida y seguridad en el flujo exitoso")
    void enviarNotificaciones_CuandoServicioDisponible_DebeInvocarFeignClient() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> adapter.enviarCorreoBienvenida("a@m.com", "Juan", "juand", "Vendedor"));
        assertDoesNotThrow(() -> adapter.enviarCorreoSeguridadCuenta(
                "a@m.com", "Juan", EventoSeguridadCuenta.CAMBIO_CONTRASENA, null));
        verify(notificationFeignClient).enviarBienvenida(any());
        verify(notificationFeignClient).enviarSeguridadCuenta(any());
    }

    @Test
    @DisplayName("Debe capturar excepciones de Feign sin interrumpir el flujo principal")
    void enviarNotificaciones_CuandoFeignFalla_NoDebePropagarExcepcion() {
        // Arrange
        doThrow(new RuntimeException("Connection refused"))
                .when(notificationFeignClient).enviarBienvenida(any());
        doThrow(new RuntimeException("Timeout"))
                .when(notificationFeignClient).enviarSeguridadCuenta(any());

        // Act & Assert
        assertDoesNotThrow(() -> adapter.enviarCorreoBienvenida("a@m.com", "Juan", "juand", "Vendedor"));
        assertDoesNotThrow(() -> adapter.enviarCorreoSeguridadCuenta(
                "a@m.com", "Juan", null, "Tmp#123"));
    }
}
