package com.invengest.notification.domain.usecase;

import com.invengest.notification.domain.model.DatosAlertaStock;
import com.invengest.notification.domain.model.DatosBienvenida;
import com.invengest.notification.domain.model.DatosCierreCaja;
import com.invengest.notification.domain.model.DatosComprobanteVenta;
import com.invengest.notification.domain.model.DatosSeguridadCuenta;
import com.invengest.notification.domain.model.MensajeCorreo;
import com.invengest.notification.domain.model.gateway.CorreoGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnviarNotificacionUseCase {

    private final CorreoGateway correoGateway;
    private final PlantillaCorreoFactory plantillaCorreoFactory;

    public String enviarBienvenida(DatosBienvenida datos) {
        MensajeCorreo mensaje = plantillaCorreoFactory.construirBienvenida(datos);
        correoGateway.enviarCorreo(mensaje);
        return mensaje.idSolicitud();
    }

    public String enviarSeguridadCuenta(DatosSeguridadCuenta datos) {
        MensajeCorreo mensaje = plantillaCorreoFactory.construirSeguridadCuenta(datos);
        correoGateway.enviarCorreo(mensaje);
        return mensaje.idSolicitud();
    }

    public String enviarComprobanteVenta(DatosComprobanteVenta datos) {
        MensajeCorreo mensaje = plantillaCorreoFactory.construirComprobanteVenta(datos);
        correoGateway.enviarCorreo(mensaje);
        return mensaje.idSolicitud();
    }

    public String enviarAlertaStock(DatosAlertaStock datos) {
        MensajeCorreo mensaje = plantillaCorreoFactory.construirAlertaStock(datos);
        correoGateway.enviarCorreo(mensaje);
        return mensaje.idSolicitud();
    }

    public String enviarCierreCaja(DatosCierreCaja datos) {
        MensajeCorreo mensaje = plantillaCorreoFactory.construirCierreCaja(datos);
        correoGateway.enviarCorreo(mensaje);
        return mensaje.idSolicitud();
    }

    public String enviarPrueba(String destinatario) {
        MensajeCorreo mensaje = plantillaCorreoFactory.construirPrueba(destinatario);
        correoGateway.enviarCorreo(mensaje);
        return mensaje.idSolicitud();
    }
}
