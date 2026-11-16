package com.invengest.auth.infrastructure.driver_adapters.feign.dto;

public record NotificacionBienvenidaRequestDTO(
        String destinatario,
        String nombre,
        String username,
        String rol
) {}
