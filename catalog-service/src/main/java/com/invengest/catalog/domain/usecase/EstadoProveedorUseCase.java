package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.gateway.EstadoProveedorGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class EstadoProveedorUseCase {

    private final EstadoProveedorGateway estadoProveedorGateway;

    public List<EstadoProveedor> obtenerTodos() {
        return estadoProveedorGateway.findAll();
    }

    public EstadoProveedor obtenerPorId(Integer id) {
        return estadoProveedorGateway.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado de Proveedor no encontrado con el ID: " + id));
    }

    public EstadoProveedor obtenerPorNombre(String nombre) {
        return estadoProveedorGateway.findByNombre(nombre)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado de Proveedor no encontrado con el nombre: " + nombre));
    }
}
