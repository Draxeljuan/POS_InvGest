package com.invengest.auth.infrastructure.driver_adapters.jpa_repository;

import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.entity.RolUsuarioData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolUsuarioDataJpaRepository extends JpaRepository<RolUsuarioData, Integer> {
    Optional<RolUsuarioData> findByNombreIgnoreCase(String nombre);
}
