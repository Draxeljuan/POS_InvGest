package com.invengest.catalog.infrastructure.driver_adapters.feign;

import com.invengest.catalog.infrastructure.driver_adapters.feign.dto.SincronizarAlertaStockRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "analytics-service",
        url = "${analytics.service.url:http://localhost:8084}",
        configuration = InternalFeignRequestInterceptor.class
)
public interface AnalyticsFeignClient {

    @PostMapping("/internal/alertas-stock")
    void sincronizarAlertaStock(@RequestBody SincronizarAlertaStockRequestDTO request);
}
