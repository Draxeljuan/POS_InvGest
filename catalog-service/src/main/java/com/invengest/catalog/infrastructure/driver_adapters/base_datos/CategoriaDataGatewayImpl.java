package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.gateway.CategoriaGateway;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.CategoriaData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.CategoriaJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ProductoJpaRepository;
import com.invengest.catalog.infrastructure.mapper.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoriaDataGatewayImpl implements CategoriaGateway {

    private final CategoriaJpaRepository categoriaJpaRepository;
    private final ProductoJpaRepository productoJpaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    public Optional<Categoria> findById(Integer idCategoria) {
        return categoriaJpaRepository.findById(idCategoria).map(categoriaMapper::toDomain);
    }

    @Override
    public List<Categoria> findAll() {
        return categoriaJpaRepository.findAll().stream().map(categoriaMapper::toDomain).toList();
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return categoriaJpaRepository.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public Categoria save(Categoria categoria) {
        CategoriaData entity = categoriaMapper.toEntity(categoria);
        return categoriaMapper.toDomain(categoriaJpaRepository.save(entity));
    }

    @Override
    public void deleteById(Integer idCategoria) {
        categoriaJpaRepository.deleteById(idCategoria);
    }

    @Override
    public boolean existsProductosByCategoriaId(Integer idCategoria) {
        return productoJpaRepository.existsByCategoria_IdCategoria(idCategoria);
    }
}
