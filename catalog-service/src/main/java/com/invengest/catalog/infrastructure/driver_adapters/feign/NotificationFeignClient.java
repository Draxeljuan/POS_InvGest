package com.invengest.catalog.infrastructure.driver_adapters.feign;

import com.invengest.catalog.infrastructure.driver_adapters.feign.dto.NotificacionAlertaStockFeignRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification.service.url:http://localhost:8085}")
public interface NotificationFeignClient {

    @PostMapping("/internal/notificaciones/alerta-stock")
    void enviarAlertaStock(@RequestBody NotificacionAlertaStockFeignRequestDTO request);
}
