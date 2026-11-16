package com.invengest.transaction.infrastructure.driver_adapters.feign;

import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.NotificacionCierreCajaFeignRequestDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.NotificacionComprobanteVentaFeignRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification.service.url:http://localhost:8085}")
public interface NotificationFeignClient {

    @PostMapping("/internal/notificaciones/comprobante-venta")
    void enviarComprobanteVenta(@RequestBody NotificacionComprobanteVentaFeignRequestDTO request);

    @PostMapping("/internal/notificaciones/cierre-caja")
    void enviarResumenCierreCaja(@RequestBody NotificacionCierreCajaFeignRequestDTO request);
}
