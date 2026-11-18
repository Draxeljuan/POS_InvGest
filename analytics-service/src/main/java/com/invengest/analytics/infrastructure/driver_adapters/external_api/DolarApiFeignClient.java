package com.invengest.analytics.infrastructure.driver_adapters.external_api;

import com.invengest.analytics.infrastructure.driver_adapters.external_api.dto.DolarApiTrmResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "dolar-api",
        url = "${dolar.api.url:https://co.dolarapi.com/v1/trm}",
        configuration = ExternalFeignConfig.class
)
public interface DolarApiFeignClient {

    @GetMapping
    DolarApiTrmResponseDTO obtenerTrmActual();
}
