package com.invengest.analytics.infrastructure.driver_adapters.base_datos;

import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.gateway.ResumenVentaGateway;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.ResumenVentasDiariasData;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.repository.ResumenVentasDiariasJpaRepository;
import com.invengest.analytics.infrastructure.mapper.ResumenVentaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumenVentaDataGatewayImpl implements ResumenVentaGateway {

    private final ResumenVentasDiariasJpaRepository jpaRepository;
    private final ResumenVentaMapper mapper;

    @Override
    public Optional<ResumenVentasDiarias> findByFecha(LocalDate fecha) {
        return jpaRepository.findById(fecha)
                .map(mapper::toDomain);
    }

    @Override
    public List<ResumenVentasDiarias> findByFechaBetween(LocalDate inicio, LocalDate fin) {
        return jpaRepository.findByFechaBetweenOrderByFechaAsc(inicio, fin).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ResumenVentasDiarias> findAll() {
        return jpaRepository.findAllByOrderByFechaDesc().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public ResumenVentasDiarias save(ResumenVentasDiarias resumen) {
        ResumenVentasDiariasData entity = mapper.toEntity(resumen);
        ResumenVentasDiariasData saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
