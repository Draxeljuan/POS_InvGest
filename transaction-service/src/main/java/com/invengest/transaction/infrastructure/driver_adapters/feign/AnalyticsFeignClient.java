package com.invengest.transaction.infrastructure.driver_adapters.feign;

import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.SincronizarAnulacionVentaFeignRequestDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.SincronizarVentaFeignRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "analytics-service",
        url = "${analytics.service.url:http://localhost:8084}",
        configuration = InternalFeignRequestInterceptor.class
)
public interface AnalyticsFeignClient {

    @PostMapping("/internal/ventas")
    void sincronizarVenta(@RequestBody SincronizarVentaFeignRequestDTO request);

    @PostMapping("/internal/ventas/anulacion")
    void sincronizarAnulacionVenta(@RequestBody SincronizarAnulacionVentaFeignRequestDTO request);
}
