package com.invengest.auth.domain.model.gateway;

import com.invengest.auth.domain.model.EventoSeguridadCuenta;

public interface NotificacionGateway {
    void enviarCorreoBienvenida(String destinatario, String nombre, String username, String rol);
    void enviarCorreoSeguridadCuenta(String destinatario, String nombre, EventoSeguridadCuenta evento, String contrasenaTemporal);
}
