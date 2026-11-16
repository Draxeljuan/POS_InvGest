package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.EntidadEnUsoException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.model.gateway.CategoriaServicioGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CategoriaServicioUseCase {

    private final CategoriaServicioGateway categoriaServicioGateway;

    public CategoriaServicio crear(CategoriaServicio categoriaServicio) {
        categoriaServicio.validarInvariantes();
        return categoriaServicioGateway.save(categoriaServicio);
    }

    public CategoriaServicio actualizar(CategoriaServicio categoriaServicio) {
        categoriaServicio.validarInvariantes();
        if (categoriaServicio.getIdCategoriaServicio() == null
                || categoriaServicioGateway.findById(categoriaServicio.getIdCategoriaServicio()).isEmpty()) {
            throw new RecursoNoEncontradoException("La categoría de servicio a actualizar no existe.");
        }
        return categoriaServicioGateway.save(categoriaServicio);
    }

    public List<CategoriaServicio> obtenerTodas() {
        return categoriaServicioGateway.findAll();
    }

    public CategoriaServicio obtenerPorId(Integer id) {
        return categoriaServicioGateway.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría de servicio no encontrada con ID: " + id));
    }

    public void eliminarPorId(Integer id) {
        categoriaServicioGateway.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La categoría de servicio no existe."));

        if (categoriaServicioGateway.existsServiciosByCategoriaServicioId(id)) {
            throw new EntidadEnUsoException("No se puede eliminar la categoría porque contiene servicios vinculados.");
        }

        categoriaServicioGateway.deleteById(id);
    }
}
