package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.gateway.EstadoProductoGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class EstadoProductoUseCase {

    private final EstadoProductoGateway estadoProductoGateway;

    public List<EstadoProducto> obtenerTodos() {
        return estadoProductoGateway.findAll();
    }

    public EstadoProducto obtenerPorId(Integer id) {
        return estadoProductoGateway.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado de Producto no encontrado con el ID: " + id));
    }

    public EstadoProducto obtenerPorNombre(String nombre) {
        return estadoProductoGateway.findByNombre(nombre)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estado de Producto no encontrado con el nombre: " + nombre));
    }
}
