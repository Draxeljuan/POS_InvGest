package com.invengest.notification.domain.model.gateway;

import com.invengest.notification.domain.model.MensajeCorreo;

public interface CorreoGateway {

    void enviarCorreo(MensajeCorreo mensaje);
}
