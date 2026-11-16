package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.gateway.ClienteGateway;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.ClienteData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.ClienteJpaRepository;
import com.invengest.transaction.infrastructure.mapper.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ClienteDataGatewayImpl implements ClienteGateway {

    private final ClienteJpaRepository repository;
    private final ClienteMapper mapper;

    @Override
    @Transactional
    public Cliente save(Cliente cliente) {
        ClienteData entity = mapper.toEntity(cliente);
        ClienteData saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> findById(Integer idCliente) {
        return repository.findById(idCliente).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> findByDocumentoIdentidad(String documentoIdentidad) {
        return repository.findByDocumentoIdentidad(documentoIdentidad).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer idCliente) {
        return repository.existsById(idCliente);
    }
}
