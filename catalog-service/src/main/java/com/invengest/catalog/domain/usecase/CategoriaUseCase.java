package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.EntidadEnUsoException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.gateway.CategoriaGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CategoriaUseCase {

    private final CategoriaGateway categoriaGateway;

    public Categoria crear(Categoria categoria) {
        categoria.validarInvariantes();
        if (categoriaGateway.existsByNombre(categoria.getNombre())) {
            throw new EntidadDuplicadaException("Ya existe una categoría con el nombre: " + categoria.getNombre());
        }
        return categoriaGateway.save(categoria);
    }

    public Categoria actualizar(Categoria categoria) {
        categoria.validarInvariantes();
        Categoria existente = categoriaGateway.findById(categoria.getIdCategoria())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La categoría con ID " + categoria.getIdCategoria() + " no existe."));

        if (!existente.getNombre().equalsIgnoreCase(categoria.getNombre())
                && categoriaGateway.existsByNombre(categoria.getNombre())) {
            throw new EntidadDuplicadaException("Ya existe una categoría con el nombre: " + categoria.getNombre());
        }
        return categoriaGateway.save(categoria);
    }

    public List<Categoria> obtenerTodas() {
        return categoriaGateway.findAll();
    }

    public Categoria obtenerPorId(Integer id) {
        return categoriaGateway.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con ID: " + id));
    }

    public void eliminarPorId(Integer id) {
        if (categoriaGateway.findById(id).isEmpty()) {
            throw new RecursoNoEncontradoException("La categoría no existe.");
        }
        if (categoriaGateway.existsProductosByCategoriaId(id)) {
            throw new EntidadEnUsoException("No se puede eliminar la categoría porque tiene productos asociados.");
        }
        categoriaGateway.deleteById(id);
    }
}
