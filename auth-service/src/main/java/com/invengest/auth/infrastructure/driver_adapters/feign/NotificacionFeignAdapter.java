package com.invengest.auth.infrastructure.driver_adapters.feign;

import com.invengest.auth.domain.model.EventoSeguridadCuenta;
import com.invengest.auth.domain.model.gateway.NotificacionGateway;
import com.invengest.auth.infrastructure.driver_adapters.feign.dto.NotificacionBienvenidaRequestDTO;
import com.invengest.auth.infrastructure.driver_adapters.feign.dto.NotificacionSeguridadRequestDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificacionFeignAdapter implements NotificacionGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificacionFeignAdapter.class);

    private final NotificationFeignClient notificationFeignClient;

    @Override
    public void enviarCorreoBienvenida(String destinatario, String nombre, String username, String rol) {
        try {
            notificationFeignClient.enviarBienvenida(
                    new NotificacionBienvenidaRequestDTO(destinatario, nombre, username, rol)
            );
        } catch (RuntimeException ex) {
            log.warn("notificacion_fallida tipo=BIENVENIDA destinatario={} username={} causa={}",
                    destinatario, username, ex.getMessage());
        }
    }

    @Override
    public void enviarCorreoSeguridadCuenta(String destinatario, String nombre, EventoSeguridadCuenta evento, String contrasenaTemporal) {
        try {
            notificationFeignClient.enviarSeguridadCuenta(
                    new NotificacionSeguridadRequestDTO(
                            destinatario,
                            nombre,
                            evento != null ? evento.name() : "SEGURIDAD",
                            contrasenaTemporal
                    )
            );
        } catch (RuntimeException ex) {
            log.warn("notificacion_fallida tipo=SEGURIDAD_CUENTA evento={} destinatario={} causa={}",
                    evento, destinatario, ex.getMessage());
        }
    }
}
