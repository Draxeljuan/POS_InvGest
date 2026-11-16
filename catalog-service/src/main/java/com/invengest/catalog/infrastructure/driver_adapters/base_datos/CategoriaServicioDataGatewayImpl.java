package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.model.gateway.CategoriaServicioGateway;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.CategoriaServicioData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.CategoriaServicioJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ServicioJpaRepository;
import com.invengest.catalog.infrastructure.mapper.CategoriaServicioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoriaServicioDataGatewayImpl implements CategoriaServicioGateway {

    private final CategoriaServicioJpaRepository categoriaServicioJpaRepository;
    private final ServicioJpaRepository servicioJpaRepository;
    private final CategoriaServicioMapper categoriaServicioMapper;

    @Override
    public Optional<CategoriaServicio> findById(Integer idCategoriaServicio) {
        return categoriaServicioJpaRepository.findById(idCategoriaServicio).map(categoriaServicioMapper::toDomain);
    }

    @Override
    public Optional<CategoriaServicio> findByNombre(String nombre) {
        return categoriaServicioJpaRepository.findByNombreIgnoreCase(nombre).map(categoriaServicioMapper::toDomain);
    }

    @Override
    public List<CategoriaServicio> findAll() {
        return categoriaServicioJpaRepository.findAll().stream().map(categoriaServicioMapper::toDomain).toList();
    }

    @Override
    public CategoriaServicio save(CategoriaServicio categoriaServicio) {
        CategoriaServicioData entity = categoriaServicioMapper.toEntity(categoriaServicio);
        return categoriaServicioMapper.toDomain(categoriaServicioJpaRepository.save(entity));
    }

    @Override
    public void deleteById(Integer idCategoriaServicio) {
        categoriaServicioJpaRepository.deleteById(idCategoriaServicio);
    }

    @Override
    public boolean existsServiciosByCategoriaServicioId(Integer idCategoriaServicio) {
        return servicioJpaRepository.existsByCategoriaServicio_IdCategoriaServicio(idCategoriaServicio);
    }
}
