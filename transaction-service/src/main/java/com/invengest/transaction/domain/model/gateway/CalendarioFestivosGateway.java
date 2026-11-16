package com.invengest.transaction.domain.model.gateway;

import com.invengest.transaction.domain.model.DiaFestivo;

import java.util.List;

public interface CalendarioFestivosGateway {
    List<DiaFestivo> obtenerFestivos(int anio);
}
