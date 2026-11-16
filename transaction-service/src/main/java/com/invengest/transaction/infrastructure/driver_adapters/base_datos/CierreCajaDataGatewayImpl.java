package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.gateway.CierreCajaGateway;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.CierreCajaData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.CierreCajaJpaRepository;
import com.invengest.transaction.infrastructure.mapper.CierreCajaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CierreCajaDataGatewayImpl implements CierreCajaGateway {

    private final CierreCajaJpaRepository repository;
    private final CierreCajaMapper mapper;

    @Override
    @Transactional
    public CierreCaja save(CierreCaja cierreCaja) {
        CierreCajaData entity = mapper.toEntity(cierreCaja);
        if (entity.getDetalles() != null) {
            entity.getDetalles().forEach(detalle -> detalle.setCierreCaja(entity));
        }
        CierreCajaData saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CierreCaja> findById(Integer idCierre) {
        return repository.findById(idCierre).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CierreCaja> findByFechaCierre(LocalDate fechaCierre) {
        return repository.findByFechaCierre(fechaCierre).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFechaCierre(LocalDate fechaCierre) {
        return repository.existsByFechaCierre(fechaCierre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CierreCaja> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
