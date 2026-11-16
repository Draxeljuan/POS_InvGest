package com.invengest.notification.domain.model;

public record DatosSeguridadCuenta(
        String destinatario,
        String nombre,
        String evento,
        String contrasenaTemporal
) {}
