package com.invengest.auth.infrastructure.driver_adapters.jpa_repository;

import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.entity.UsuarioData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioDataJpaRepository extends JpaRepository<UsuarioData, Integer> {
    Optional<UsuarioData> findByNombreUsuario(String nombreUsuario);
    Optional<UsuarioData> findByEmailIgnoreCase(String email);
    List<UsuarioData> findByRolNombreIgnoreCaseAndActivoTrue(String nombreRol);
}
