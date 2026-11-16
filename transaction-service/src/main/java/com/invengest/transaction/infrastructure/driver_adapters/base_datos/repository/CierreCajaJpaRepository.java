package com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.CierreCajaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface CierreCajaJpaRepository extends JpaRepository<CierreCajaData, Integer> {

    @Query("SELECT DISTINCT c FROM CierreCajaData c LEFT JOIN FETCH c.detalles d LEFT JOIN FETCH d.metodoPago WHERE c.fechaCierre = :fechaCierre")
    Optional<CierreCajaData> findByFechaCierre(@Param("fechaCierre") LocalDate fechaCierre);

    boolean existsByFechaCierre(LocalDate fechaCierre);
}
