package com.invengest.catalog.domain.model.gateway;

import com.invengest.catalog.domain.model.EstadoProveedor;

import java.util.List;
import java.util.Optional;

public interface EstadoProveedorGateway {
    Optional<EstadoProveedor> findById(Integer idEstadoProveedor);
    List<EstadoProveedor> findAll();
    Optional<EstadoProveedor> findByNombre(String nombre);
}
