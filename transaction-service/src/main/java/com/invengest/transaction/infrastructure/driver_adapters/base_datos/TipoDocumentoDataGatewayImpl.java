package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.domain.model.gateway.TipoDocumentoGateway;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.TipoDocumentoJpaRepository;
import com.invengest.transaction.infrastructure.mapper.TipoDocumentoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TipoDocumentoDataGatewayImpl implements TipoDocumentoGateway {

    private final TipoDocumentoJpaRepository repository;
    private final TipoDocumentoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoDocumento> findById(Integer idTipoDocumento) {
        return repository.findById(idTipoDocumento).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumento> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
