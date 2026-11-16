package com.invengest.analytics.domain.usecase;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.DiaFestivo;
import com.invengest.analytics.domain.model.ReporteEstacionalidad;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.gateway.CalendarioFestivosGateway;
import com.invengest.analytics.domain.model.gateway.ResumenVentaGateway;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class ConsultarEstacionalidadUseCase {

    private final ResumenVentaGateway resumenVentaGateway;
    private final CalendarioFestivosGateway calendarioFestivosGateway;

    public ConsultarEstacionalidadUseCase(
            ResumenVentaGateway resumenVentaGateway,
            CalendarioFestivosGateway calendarioFestivosGateway) {
        this.resumenVentaGateway = resumenVentaGateway;
        this.calendarioFestivosGateway = calendarioFestivosGateway;
    }

    public ReporteEstacionalidad analizarEstacionalidadMensual(int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new ReglaNegocioException("El mes debe estar comprendido entre 1 y 12.");
        }
        if (anio < 2000 || anio > 2100) {
            throw new ReglaNegocioException("El año especificado no es válido.");
        }

        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate inicioMes = yearMonth.atDay(1);
        LocalDate finMes = yearMonth.atEndOfMonth();

        List<ResumenVentasDiarias> resumenesMes = resumenVentaGateway.findByFechaBetween(inicioMes, finMes);
        List<DiaFestivo> festivosAnio = calendarioFestivosGateway.obtenerFestivos(anio);

        return ReporteEstacionalidad.calcular(anio, mes, resumenesMes, festivosAnio);
    }
}
