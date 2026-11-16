package com.invengest.catalog.domain.model.gateway;

import com.invengest.catalog.domain.model.CategoriaServicio;

import java.util.List;
import java.util.Optional;

public interface CategoriaServicioGateway {
    Optional<CategoriaServicio> findById(Integer idCategoriaServicio);
    Optional<CategoriaServicio> findByNombre(String nombre);
    List<CategoriaServicio> findAll();
    CategoriaServicio save(CategoriaServicio categoriaServicio);
    void deleteById(Integer idCategoriaServicio);
    boolean existsServiciosByCategoriaServicioId(Integer idCategoriaServicio);
}
