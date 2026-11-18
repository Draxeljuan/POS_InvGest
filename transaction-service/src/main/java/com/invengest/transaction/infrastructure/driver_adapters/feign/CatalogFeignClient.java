package com.invengest.transaction.infrastructure.driver_adapters.feign;

import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.CatalogProductoFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.CatalogServicioFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ItemStockFeignRequestDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ItemStockValidadoFeignDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "catalog-service",
        url = "${catalog.service.url:http://localhost:8082}",
        configuration = InternalFeignRequestInterceptor.class
)
public interface CatalogFeignClient {

    @PostMapping("/internal/productos/validar-stock")
    List<ItemStockValidadoFeignDTO> validarStockBatch(@RequestBody List<ItemStockFeignRequestDTO> items);

    @PostMapping("/internal/productos/descontar-stock")
    void descontarStockBatch(@RequestBody List<ItemStockFeignRequestDTO> items);

    @PostMapping("/internal/productos/reintegrar-stock")
    void reintegrarStockBatch(@RequestBody List<ItemStockFeignRequestDTO> items);

    @GetMapping("/api/catalog/productos/{id}")
    CatalogProductoFeignDTO obtenerProductoPorId(@PathVariable("id") String id);

    @GetMapping("/api/catalog/servicios/{id}")
    CatalogServicioFeignDTO obtenerServicioPorId(@PathVariable("id") Integer id);
}
