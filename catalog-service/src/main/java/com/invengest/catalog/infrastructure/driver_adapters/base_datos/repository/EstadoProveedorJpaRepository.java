package com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.EstadoProveedorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoProveedorJpaRepository extends JpaRepository<EstadoProveedorData, Integer> {
    Optional<EstadoProveedorData> findByNombreIgnoreCase(String nombre);
}
