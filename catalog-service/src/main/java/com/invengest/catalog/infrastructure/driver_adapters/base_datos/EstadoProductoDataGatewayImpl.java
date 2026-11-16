package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.gateway.EstadoProductoGateway;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.EstadoProductoJpaRepository;
import com.invengest.catalog.infrastructure.mapper.EstadoProductoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EstadoProductoDataGatewayImpl implements EstadoProductoGateway {

    private final EstadoProductoJpaRepository estadoProductoJpaRepository;
    private final EstadoProductoMapper estadoProductoMapper;

    @Override
    public Optional<EstadoProducto> findById(Integer idEstado) {
        return estadoProductoJpaRepository.findById(idEstado).map(estadoProductoMapper::toDomain);
    }

    @Override
    public Optional<EstadoProducto> findByNombre(String nombre) {
        return estadoProductoJpaRepository.findByNombreIgnoreCase(nombre).map(estadoProductoMapper::toDomain);
    }

    @Override
    public List<EstadoProducto> findAll() {
        return estadoProductoJpaRepository.findAll().stream().map(estadoProductoMapper::toDomain).toList();
    }
}
