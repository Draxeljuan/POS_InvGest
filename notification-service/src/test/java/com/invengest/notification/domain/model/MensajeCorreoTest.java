package com.invengest.notification.domain.model;

import com.invengest.notification.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MensajeCorreoTest {

    @Test
    @DisplayName("Debe construir MensajeCorreo válido con copia inmutable de destinatarios")
    void constructor_CuandoDatosValidos_DebeConstruirMensaje() {
        // Arrange
        List<String> destinatarios = List.of("destino@papeleria.local");

        // Act
        MensajeCorreo mensaje = new MensajeCorreo(
                "id-1",
                destinatarios,
                "Asunto",
                "Cuerpo",
                TipoNotificacion.PRUEBA
        );

        // Assert
        assertEquals("id-1", mensaje.idSolicitud());
        assertEquals(List.of("destino@papeleria.local"), mensaje.destinatarios());
        assertEquals("Asunto", mensaje.asunto());
        assertEquals("Cuerpo", mensaje.cuerpo());
        assertEquals(TipoNotificacion.PRUEBA, mensaje.tipoNotificacion());
    }

    @Test
    @DisplayName("Debe validar invariantes obligatorios y lanzar ReglaNegocioException")
    void constructor_CuandoDatosInvalidos_DebeLanzarReglaNegocioException() {
        // Arrange
        List<String> destinatariosValidos = List.of("destino@papeleria.local");

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo(" ", destinatariosValidos, "Asunto", "Cuerpo", TipoNotificacion.PRUEBA));
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo("id-1", List.of(), "Asunto", "Cuerpo", TipoNotificacion.PRUEBA));
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo("id-1", List.of("   "), "Asunto", "Cuerpo", TipoNotificacion.PRUEBA));
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo("id-1", destinatariosValidos, "", "Cuerpo", TipoNotificacion.PRUEBA));
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo("id-1", destinatariosValidos, "Asunto", "  ", TipoNotificacion.PRUEBA));
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo("id-1", destinatariosValidos, "Asunto", "Cuerpo", null));
    }
}
