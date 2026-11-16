package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.MovimientoInventario;
import com.invengest.transaction.domain.model.gateway.MovimientoInventarioGateway;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.MovimientoInventarioData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.MovimientoInventarioJpaRepository;
import com.invengest.transaction.infrastructure.mapper.MovimientoInventarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MovimientoInventarioDataGatewayImpl implements MovimientoInventarioGateway {

    private final MovimientoInventarioJpaRepository repository;
    private final MovimientoInventarioMapper mapper;

    @Override
    @Transactional
    public MovimientoInventario save(MovimientoInventario movimiento) {
        MovimientoInventarioData entity = mapper.toEntity(movimiento);
        MovimientoInventarioData saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MovimientoInventario> findById(Integer idMovimientoInventario) {
        return repository.findById(idMovimientoInventario).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> findByIdProducto(String idProducto) {
        return repository.findByIdProductoOrderByFechaMovimientoDesc(idProducto).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin) {
        return repository.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(inicio, fin).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
