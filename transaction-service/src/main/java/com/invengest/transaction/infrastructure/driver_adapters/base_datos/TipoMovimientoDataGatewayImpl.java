package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.gateway.TipoMovimientoGateway;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.TipoMovimientoJpaRepository;
import com.invengest.transaction.infrastructure.mapper.TipoMovimientoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TipoMovimientoDataGatewayImpl implements TipoMovimientoGateway {

    private final TipoMovimientoJpaRepository repository;
    private final TipoMovimientoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoMovimiento> findById(Integer idMovimiento) {
        return repository.findById(idMovimiento).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoMovimiento> findByNombre(String nombre) {
        return repository.findByNombre(nombre).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoMovimiento> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
