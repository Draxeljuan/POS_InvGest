package com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository;

import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.TipoDocumentoData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoDocumentoJpaRepository extends JpaRepository<TipoDocumentoData, Integer> {
}
