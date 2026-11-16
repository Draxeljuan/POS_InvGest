package com.invengest.auth.infrastructure.driver_adapters.feign.dto;

public record NotificacionSeguridadRequestDTO(
        String destinatario,
        String nombre,
        String evento,
        String contrasenaTemporal
) {}
