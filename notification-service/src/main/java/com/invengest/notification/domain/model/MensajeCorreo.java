package com.invengest.notification.domain.model;

import com.invengest.notification.domain.exception.ReglaNegocioException;

import java.util.List;

public record MensajeCorreo(
        String idSolicitud,
        List<String> destinatarios,
        String asunto,
        String cuerpo,
        TipoNotificacion tipoNotificacion
) {
    public MensajeCorreo {
        if (idSolicitud == null || idSolicitud.isBlank()) {
            throw new ReglaNegocioException("El identificador de solicitud de notificación es obligatorio");
        }
        if (destinatarios == null || destinatarios.isEmpty()) {
            throw new ReglaNegocioException("Debe especificar al menos un destinatario de correo electrónico");
        }
        List<String> limpios = destinatarios.stream()
                .filter(d -> d != null && !d.isBlank())
                .map(String::trim)
                .toList();
        if (limpios.isEmpty()) {
            throw new ReglaNegocioException("La lista de destinatarios no contiene correos válidos");
        }
        if (asunto == null || asunto.isBlank()) {
            throw new ReglaNegocioException("El asunto del correo no puede estar vacío");
        }
        if (cuerpo == null || cuerpo.isBlank()) {
            throw new ReglaNegocioException("El cuerpo del correo no puede estar vacío");
        }
        if (tipoNotificacion == null) {
            throw new ReglaNegocioException("El tipo de notificación es obligatorio");
        }
        destinatarios = List.copyOf(limpios);
    }
}
