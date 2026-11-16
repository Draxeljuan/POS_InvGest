package com.invengest.notification.domain.usecase;

import com.invengest.notification.domain.model.DatosAlertaStock;
import com.invengest.notification.domain.model.DatosBienvenida;
import com.invengest.notification.domain.model.DatosCierreCaja;
import com.invengest.notification.domain.model.DatosComprobanteVenta;
import com.invengest.notification.domain.model.DatosSeguridadCuenta;
import com.invengest.notification.domain.model.ItemAlertaStock;
import com.invengest.notification.domain.model.ItemComprobante;
import com.invengest.notification.domain.model.MensajeCorreo;
import com.invengest.notification.domain.model.gateway.CorreoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnviarNotificacionUseCaseTest {

    @Mock
    private CorreoGateway correoGateway;

    private EnviarNotificacionUseCase enviarNotificacionUseCase;

    @BeforeEach
    void setUp() {
        PlantillaCorreoFactory factory = new PlantillaCorreoFactory();
        enviarNotificacionUseCase = new EnviarNotificacionUseCase(correoGateway, factory);
    }

    @Test
    @DisplayName("enviarBienvenida construye el mensaje y delega al CorreoGateway")
    void enviarBienvenida_Exitoso() {
        // Arrange
        DatosBienvenida datos = new DatosBienvenida("user@test.com", "Juan", "juanp", "Vendedor");
        ArgumentCaptor<MensajeCorreo> captor = ArgumentCaptor.forClass(MensajeCorreo.class);

        // Act
        String idSolicitud = enviarNotificacionUseCase.enviarBienvenida(datos);

        // Assert
        assertNotNull(idSolicitud);
        verify(correoGateway).enviarCorreo(captor.capture());
        assertEquals(idSolicitud, captor.getValue().idSolicitud());
    }

    @Test
    @DisplayName("enviarSeguridadCuenta construye el mensaje y delega al CorreoGateway")
    void enviarSeguridadCuenta_Exitoso() {
        // Arrange
        DatosSeguridadCuenta datos = new DatosSeguridadCuenta(
                "user@test.com", "Juan", "CAMBIO_CONTRASENA", null);
        ArgumentCaptor<MensajeCorreo> captor = ArgumentCaptor.forClass(MensajeCorreo.class);

        // Act
        String idSolicitud = enviarNotificacionUseCase.enviarSeguridadCuenta(datos);

        // Assert
        assertNotNull(idSolicitud);
        verify(correoGateway).enviarCorreo(captor.capture());
        assertEquals(idSolicitud, captor.getValue().idSolicitud());
    }

    @Test
    @DisplayName("enviarComprobanteVenta construye el mensaje y delega al CorreoGateway")
    void enviarComprobanteVenta_Exitoso() {
        // Arrange
        DatosComprobanteVenta datos = new DatosComprobanteVenta(
                "cliente@test.com",
                "María",
                12,
                "2026-11-16",
                List.of(new ItemComprobante("Borrador", 1, new BigDecimal("1500"), new BigDecimal("1500"))),
                new BigDecimal("1500"),
                "Efectivo"
        );
        ArgumentCaptor<MensajeCorreo> captor = ArgumentCaptor.forClass(MensajeCorreo.class);

        // Act
        String idSolicitud = enviarNotificacionUseCase.enviarComprobanteVenta(datos);

        // Assert
        assertNotNull(idSolicitud);
        verify(correoGateway).enviarCorreo(captor.capture());
        assertEquals(idSolicitud, captor.getValue().idSolicitud());
    }

    @Test
    @DisplayName("enviarAlertaStock construye el mensaje y delega al CorreoGateway")
    void enviarAlertaStock_Exitoso() {
        // Arrange
        DatosAlertaStock datos = new DatosAlertaStock(
                List.of("admin@test.com"),
                List.of(new ItemAlertaStock("PROD-10", "Marcador", 1, 5, "Bajo Stock"))
        );
        ArgumentCaptor<MensajeCorreo> captor = ArgumentCaptor.forClass(MensajeCorreo.class);

        // Act
        String idSolicitud = enviarNotificacionUseCase.enviarAlertaStock(datos);

        // Assert
        assertNotNull(idSolicitud);
        verify(correoGateway).enviarCorreo(captor.capture());
        assertEquals(idSolicitud, captor.getValue().idSolicitud());
    }

    @Test
    @DisplayName("enviarCierreCaja construye el mensaje y delega al CorreoGateway")
    void enviarCierreCaja_Exitoso() {
        // Arrange
        DatosCierreCaja datos = new DatosCierreCaja(
                "admin@test.com",
                "2026-11-16",
                List.of(),
                new BigDecimal("50000"),
                new BigDecimal("50000"),
                BigDecimal.ZERO,
                true,
                "2026-11-17"
        );
        ArgumentCaptor<MensajeCorreo> captor = ArgumentCaptor.forClass(MensajeCorreo.class);

        // Act
        String idSolicitud = enviarNotificacionUseCase.enviarCierreCaja(datos);

        // Assert
        assertNotNull(idSolicitud);
        verify(correoGateway).enviarCorreo(captor.capture());
        assertEquals(idSolicitud, captor.getValue().idSolicitud());
    }

    @Test
    @DisplayName("enviarPrueba construye el mensaje y delega al CorreoGateway")
    void enviarPrueba_Exitoso() {
        // Arrange
        ArgumentCaptor<MensajeCorreo> captor = ArgumentCaptor.forClass(MensajeCorreo.class);

        // Act
        String idSolicitud = enviarNotificacionUseCase.enviarPrueba("admin@test.com");

        // Assert
        assertNotNull(idSolicitud);
        verify(correoGateway).enviarCorreo(captor.capture());
        assertEquals(idSolicitud, captor.getValue().idSolicitud());
    }
}
