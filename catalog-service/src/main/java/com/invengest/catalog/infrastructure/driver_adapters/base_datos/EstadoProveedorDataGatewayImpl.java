package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.gateway.EstadoProveedorGateway;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.EstadoProveedorJpaRepository;
import com.invengest.catalog.infrastructure.mapper.EstadoProveedorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EstadoProveedorDataGatewayImpl implements EstadoProveedorGateway {

    private final EstadoProveedorJpaRepository estadoProveedorJpaRepository;
    private final EstadoProveedorMapper estadoProveedorMapper;

    @Override
    public Optional<EstadoProveedor> findById(Integer idEstado) {
        return estadoProveedorJpaRepository.findById(idEstado).map(estadoProveedorMapper::toDomain);
    }

    @Override
    public Optional<EstadoProveedor> findByNombre(String nombre) {
        return estadoProveedorJpaRepository.findByNombreIgnoreCase(nombre).map(estadoProveedorMapper::toDomain);
    }

    @Override
    public List<EstadoProveedor> findAll() {
        return estadoProveedorJpaRepository.findAll().stream().map(estadoProveedorMapper::toDomain).toList();
    }
}
