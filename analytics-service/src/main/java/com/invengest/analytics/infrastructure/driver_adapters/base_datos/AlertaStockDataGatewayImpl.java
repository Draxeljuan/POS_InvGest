package com.invengest.analytics.infrastructure.driver_adapters.base_datos;

import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.gateway.AlertaStockGateway;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.AlertaStockData;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.repository.AlertaStockJpaRepository;
import com.invengest.analytics.infrastructure.mapper.AlertaStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertaStockDataGatewayImpl implements AlertaStockGateway {

    private final AlertaStockJpaRepository jpaRepository;
    private final AlertaStockMapper mapper;

    @Override
    public List<AlertaStock> findAll() {
        return jpaRepository.findAllByOrderByFechaAlertaDesc().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AlertaStock> findByEstado(String estado) {
        return jpaRepository.findByEstadoOrderByFechaAlertaDesc(estado).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AlertaStock> findRecentAlerts(int limit) {
        return jpaRepository.findAllByOrderByFechaAlertaDesc(PageRequest.of(0, limit)).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<AlertaStock> findByIdProducto(String idProducto) {
        return jpaRepository.findFirstByIdProducto(idProducto)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public AlertaStock save(AlertaStock alertaStock) {
        AlertaStockData entity = mapper.toEntity(alertaStock);
        AlertaStockData saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteByIdProducto(String idProducto) {
        jpaRepository.deleteByIdProducto(idProducto);
    }
}
