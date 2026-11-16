package com.invengest.catalog.infrastructure.driver_adapters.feign.dto;

public record ContactoUsuarioDTO(
        Integer idUsuario,
        String nombreCompleto,
        String email
) {}
