package com.invengest.notification.domain.model;

public record DatosBienvenida(
        String destinatario,
        String nombre,
        String username,
        String rol
) {}
