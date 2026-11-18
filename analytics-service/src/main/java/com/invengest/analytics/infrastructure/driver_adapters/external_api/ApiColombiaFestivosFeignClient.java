package com.invengest.analytics.infrastructure.driver_adapters.external_api;

import com.invengest.analytics.infrastructure.driver_adapters.external_api.dto.ApiColombiaHolidayDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "festivos-api",
        url = "${festivos.api.url:https://api-colombia.com/api/v1/Holiday/year}",
        configuration = ExternalFeignConfig.class
)
public interface ApiColombiaFestivosFeignClient {

    @GetMapping("/{anio}")
    List<ApiColombiaHolidayDTO> obtenerFestivosPorAnio(@PathVariable("anio") int anio);
}
