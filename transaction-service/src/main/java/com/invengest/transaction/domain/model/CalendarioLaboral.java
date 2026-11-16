package com.invengest.transaction.domain.model;

import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.gateway.CalendarioFestivosGateway;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CalendarioLaboral {

    private final CalendarioFestivosGateway calendarioFestivosGateway;

    public boolean esDiaHabil(LocalDate fecha) {
        if (fecha == null) {
            throw new ReglaNegocioException("La fecha a validar no puede ser nula");
        }
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }
        Set<LocalDate> festivosAnio = obtenerConjuntoFestivos(fecha.getYear());
        return !festivosAnio.contains(fecha);
    }

    public LocalDate siguienteDiaHabil(LocalDate fecha) {
        if (fecha == null) {
            throw new ReglaNegocioException("La fecha base no puede ser nula");
        }
        Map<Integer, Set<LocalDate>> festivosPorAnio = new HashMap<>();
        LocalDate candidato = fecha.plusDays(1);
        for (int i = 0; i < 366; i++) {
            if (candidato.getDayOfWeek() != DayOfWeek.SUNDAY) {
                Set<LocalDate> festivos = festivosPorAnio.computeIfAbsent(
                        candidato.getYear(),
                        this::obtenerConjuntoFestivos
                );
                if (!festivos.contains(candidato)) {
                    return candidato;
                }
            }
            candidato = candidato.plusDays(1);
        }
        return candidato;
    }

    public int contarDiasHabiles(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new ReglaNegocioException("Las fechas inicial y final son obligatorias");
        }
        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha final no puede ser anterior a la fecha inicial");
        }
        Map<Integer, Set<LocalDate>> festivosPorAnio = new HashMap<>();
        int contador = 0;
        LocalDate cursor = desde;
        while (!cursor.isAfter(hasta)) {
            if (cursor.getDayOfWeek() != DayOfWeek.SUNDAY) {
                Set<LocalDate> festivos = festivosPorAnio.computeIfAbsent(
                        cursor.getYear(),
                        this::obtenerConjuntoFestivos
                );
                if (!festivos.contains(cursor)) {
                    contador++;
                }
            }
            cursor = cursor.plusDays(1);
        }
        return contador;
    }

    private Set<LocalDate> obtenerConjuntoFestivos(int anio) {
        List<DiaFestivo> festivos = calendarioFestivosGateway.obtenerFestivos(anio);
        if (festivos == null || festivos.isEmpty()) {
            return Set.of();
        }
        return festivos.stream()
                .map(DiaFestivo::getFecha)
                .filter(f -> f != null)
                .collect(Collectors.toSet());
    }
}
