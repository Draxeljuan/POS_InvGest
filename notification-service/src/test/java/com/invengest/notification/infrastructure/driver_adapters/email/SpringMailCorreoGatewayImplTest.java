package com.invengest.notification.infrastructure.driver_adapters.email;

import com.invengest.notification.domain.model.MensajeCorreo;
import com.invengest.notification.domain.model.TipoNotificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpringMailCorreoGatewayImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("enviarCorreo envía SimpleMailMessage con remitente configurado")
    void enviarCorreo_ConRemitente_EnviaExitosamente() {
        // Arrange
        SpringMailCorreoGatewayImpl gateway = new SpringMailCorreoGatewayImpl(
                javaMailSender,
                "no-reply@invengest.local"
        );
        MensajeCorreo mensaje = new MensajeCorreo(
                "uuid-123",
                List.of("admin@papeleria.local", "soporte@papeleria.local"),
                "Asunto de prueba",
                "Cuerpo del mensaje",
                TipoNotificacion.PRUEBA
        );
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        gateway.enviarCorreo(mensaje);

        // Assert
        verify(javaMailSender).send(captor.capture());
        SimpleMailMessage enviado = captor.getValue();
        assertEquals("no-reply@invengest.local", enviado.getFrom());
        assertArrayEquals(new String[]{"admin@papeleria.local", "soporte@papeleria.local"}, enviado.getTo());
        assertEquals("Asunto de prueba", enviado.getSubject());
        assertEquals("Cuerpo del mensaje", enviado.getText());
    }

    @Test
    @DisplayName("enviarCorreo omite campo from cuando no está configurado")
    void enviarCorreo_SinRemitente_EnviaSinFrom() {
        // Arrange
        SpringMailCorreoGatewayImpl gateway = new SpringMailCorreoGatewayImpl(javaMailSender, "   ");
        MensajeCorreo mensaje = new MensajeCorreo(
                "uuid-456",
                List.of("cliente@papeleria.local"),
                "Comprobante",
                "Detalle",
                TipoNotificacion.COMPROBANTE_VENTA
        );
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        gateway.enviarCorreo(mensaje);

        // Assert
        verify(javaMailSender).send(captor.capture());
        assertNull(captor.getValue().getFrom());
    }

    @Test
    @DisplayName("enviarCorreo captura MailException sin propagar error al hilo asíncrono")
    void enviarCorreo_FalloSmtpMailException_NoPropagaExcepcion() {
        // Arrange
        SpringMailCorreoGatewayImpl gateway = new SpringMailCorreoGatewayImpl(
                javaMailSender,
                "no-reply@invengest.local"
        );
        MensajeCorreo mensaje = new MensajeCorreo(
                "uuid-err-1",
                List.of("destino@papeleria.local"),
                "Asunto",
                "Cuerpo",
                TipoNotificacion.BIENVENIDA
        );
        doThrow(new MailSendException("SMTP timeout")).when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> gateway.enviarCorreo(mensaje));
    }

    @Test
    @DisplayName("enviarCorreo captura cualquier RuntimeException inesperada sin propagarla")
    void enviarCorreo_FalloInesperado_NoPropagaExcepcion() {
        // Arrange
        SpringMailCorreoGatewayImpl gateway = new SpringMailCorreoGatewayImpl(javaMailSender, null);
        MensajeCorreo mensaje = new MensajeCorreo(
                "uuid-err-2",
                List.of("destino@papeleria.local"),
                "Asunto",
                "Cuerpo",
                TipoNotificacion.ALERTA_STOCK
        );
        doThrow(new RuntimeException("Error inesperado")).when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> gateway.enviarCorreo(mensaje));
    }
}
