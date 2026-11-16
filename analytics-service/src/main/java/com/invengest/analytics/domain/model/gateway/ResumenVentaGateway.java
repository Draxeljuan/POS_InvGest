package com.invengest.analytics.domain.model.gateway;

import com.invengest.analytics.domain.model.ResumenVentasDiarias;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ResumenVentaGateway {
    Optional<ResumenVentasDiarias> findByFecha(LocalDate fecha);
    List<ResumenVentasDiarias> findByFechaBetween(LocalDate inicio, LocalDate fin);
    List<ResumenVentasDiarias> findAll();
    ResumenVentasDiarias save(ResumenVentasDiarias resumen);
}
