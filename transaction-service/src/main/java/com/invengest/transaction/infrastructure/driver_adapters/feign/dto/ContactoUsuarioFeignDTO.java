package com.invengest.transaction.infrastructure.driver_adapters.feign.dto;

public record ContactoUsuarioFeignDTO(
        Integer idUsuario,
        String nombreCompleto,
        String email
) {}
