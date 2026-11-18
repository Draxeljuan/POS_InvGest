package com.invengest.transaction.infrastructure.driver_adapters.feign;

import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ContactoUsuarioFeignDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "auth-service",
        url = "${auth.service.url:http://localhost:8081}",
        configuration = InternalFeignRequestInterceptor.class
)
public interface AuthFeignClient {

    @GetMapping("/internal/usuarios/administradores/contacto")
    List<ContactoUsuarioFeignDTO> obtenerContactosAdministradores();
}
