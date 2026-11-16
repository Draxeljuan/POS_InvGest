package com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.CategoriaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaJpaRepository extends JpaRepository<CategoriaData, Integer> {
    boolean existsByNombreIgnoreCase(String nombre);
}
