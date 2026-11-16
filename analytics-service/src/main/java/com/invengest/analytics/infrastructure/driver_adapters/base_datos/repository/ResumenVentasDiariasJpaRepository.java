package com.invengest.analytics.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.ResumenVentasDiariasData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ResumenVentasDiariasJpaRepository extends JpaRepository<ResumenVentasDiariasData, LocalDate> {
    List<ResumenVentasDiariasData> findByFechaBetweenOrderByFechaAsc(LocalDate inicio, LocalDate fin);
    List<ResumenVentasDiariasData> findAllByOrderByFechaDesc();
}
