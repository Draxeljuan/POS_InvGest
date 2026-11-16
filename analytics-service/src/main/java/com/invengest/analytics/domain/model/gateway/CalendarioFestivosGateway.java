package com.invengest.analytics.domain.model.gateway;

import com.invengest.analytics.domain.model.DiaFestivo;

import java.util.List;

public interface CalendarioFestivosGateway {
    List<DiaFestivo> obtenerFestivos(int anio);
}
