package com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.MetodoPagoData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetodoPagoJpaRepository extends JpaRepository<MetodoPagoData, Integer> {
    Optional<MetodoPagoData> findByNombre(String nombre);
}
