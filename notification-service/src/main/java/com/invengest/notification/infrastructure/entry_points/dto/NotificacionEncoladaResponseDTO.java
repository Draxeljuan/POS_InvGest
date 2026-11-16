package com.invengest.notification.infrastructure.entry_points.dto;

public record NotificacionEncoladaResponseDTO(
        String idSolicitud,
        String estado
) {
    public static final String ESTADO_ENCOLADA = "ENCOLADA";

    public static NotificacionEncoladaResponseDTO encolada(String idSolicitud) {
        return new NotificacionEncoladaResponseDTO(idSolicitud, ESTADO_ENCOLADA);
    }
}
