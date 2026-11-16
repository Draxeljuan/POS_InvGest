package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.VentaData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.VentaJpaRepository;
import com.invengest.transaction.infrastructure.mapper.VentaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VentaDataGatewayImpl implements VentaGateway {

    private final VentaJpaRepository ventaJpaRepository;
    private final VentaMapper ventaMapper;

    @Override
    @Transactional
    public Venta save(Venta venta) {
        VentaData entity = ventaMapper.toEntity(venta);
        if (entity.getDetalles() != null) {
            entity.getDetalles().forEach(detalle -> detalle.setVenta(entity));
        }
        VentaData saved = ventaJpaRepository.save(entity);
        return ventaMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Venta> findById(Integer idVenta) {
        return ventaJpaRepository.findByIdWithDetails(idVenta)
                .or(() -> ventaJpaRepository.findById(idVenta))
                .map(ventaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> findAll() {
        return ventaJpaRepository.findAll().stream()
                .map(ventaMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin) {
        return ventaJpaRepository.findByFechaBetween(inicio, fin).stream()
                .map(ventaMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> findByIdUsuario(Integer idUsuario) {
        return ventaJpaRepository.findByIdUsuario(idUsuario).stream()
                .map(ventaMapper::toDomain)
                .toList();
    }
}
