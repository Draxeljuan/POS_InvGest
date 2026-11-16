package com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.EstadoProductoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoProductoJpaRepository extends JpaRepository<EstadoProductoData, Integer> {
    Optional<EstadoProductoData> findByNombreIgnoreCase(String nombre);
}
