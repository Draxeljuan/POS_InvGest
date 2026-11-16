package com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.DetalleVentaData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleVentaJpaRepository extends JpaRepository<DetalleVentaData, Integer> {
    List<DetalleVentaData> findByVentaIdVenta(Integer idVenta);
}
