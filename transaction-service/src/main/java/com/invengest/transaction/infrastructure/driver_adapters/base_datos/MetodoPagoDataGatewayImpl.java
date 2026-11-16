package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.gateway.MetodoPagoGateway;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.MetodoPagoJpaRepository;
import com.invengest.transaction.infrastructure.mapper.MetodoPagoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MetodoPagoDataGatewayImpl implements MetodoPagoGateway {

    private final MetodoPagoJpaRepository repository;
    private final MetodoPagoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<MetodoPago> findById(Integer idMetodoPago) {
        return repository.findById(idMetodoPago).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetodoPago> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer idMetodoPago) {
        return repository.existsById(idMetodoPago);
    }
}
