package com.invengest.auth.infrastructure.driver_adapters.feign;

import com.invengest.auth.infrastructure.driver_adapters.feign.dto.NotificacionBienvenidaRequestDTO;
import com.invengest.auth.infrastructure.driver_adapters.feign.dto.NotificacionSeguridadRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "${notification.service.url:http://localhost:8085}"
)
public interface NotificationFeignClient {

    @PostMapping("/internal/notificaciones/bienvenida")
    void enviarBienvenida(@RequestBody NotificacionBienvenidaRequestDTO request);

    @PostMapping("/internal/notificaciones/seguridad-cuenta")
    void enviarSeguridadCuenta(@RequestBody NotificacionSeguridadRequestDTO request);
}
