package com.invengest.catalog.infrastructure.entry_points.dto.response;

public record ProveedorResponse(
        Integer idProveedor,
        EstadoProveedorResponse estado,
        String nombre,
        String telefono,
        String email,
        String direccion,
        String nit
) {}
