package com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.ClienteData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteJpaRepository extends JpaRepository<ClienteData, Integer> {
    Optional<ClienteData> findByDocumentoIdentidad(String documentoIdentidad);
}
