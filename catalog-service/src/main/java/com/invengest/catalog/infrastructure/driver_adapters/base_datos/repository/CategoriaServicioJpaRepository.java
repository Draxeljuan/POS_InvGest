package com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.CategoriaServicioData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaServicioJpaRepository extends JpaRepository<CategoriaServicioData, Integer> {
    Optional<CategoriaServicioData> findByNombreIgnoreCase(String nombre);
}
