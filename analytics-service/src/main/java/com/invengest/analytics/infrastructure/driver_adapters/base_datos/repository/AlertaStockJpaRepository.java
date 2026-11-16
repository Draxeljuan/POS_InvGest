package com.invengest.analytics.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.AlertaStockData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaStockJpaRepository extends JpaRepository<AlertaStockData, Integer> {
    List<AlertaStockData> findAllByOrderByFechaAlertaDesc();
    List<AlertaStockData> findByEstadoOrderByFechaAlertaDesc(String estado);
    List<AlertaStockData> findAllByOrderByFechaAlertaDesc(Pageable pageable);
    Optional<AlertaStockData> findFirstByIdProducto(String idProducto);
    void deleteByIdProducto(String idProducto);
}
