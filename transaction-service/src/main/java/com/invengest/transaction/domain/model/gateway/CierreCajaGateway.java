package com.invengest.transaction.domain.model.gateway;

import com.invengest.transaction.domain.model.CierreCaja;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CierreCajaGateway {
    CierreCaja save(CierreCaja cierreCaja);
    Optional<CierreCaja> findById(Integer idCierre);
    Optional<CierreCaja> findByFechaCierre(LocalDate fechaCierre);
    boolean existsByFechaCierre(LocalDate fechaCierre);
    List<CierreCaja> findAll();
}
