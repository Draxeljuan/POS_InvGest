package com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ServicioData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioJpaRepository extends JpaRepository<ServicioData, Integer> {
    List<ServicioData> findByCategoriaServicio_IdCategoriaServicio(Integer idCategoriaServicio);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByCategoriaServicio_IdCategoriaServicio(Integer idCategoriaServicio);
}
