package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteEstacionalidad {

    private int anio;
    private int mes;
    private int diasHabiles;
    private int diasNoHabiles;
    private BigDecimal ventasDiasHabiles;
    private BigDecimal ventasDiasNoHabiles;
    private int operacionesDiasHabiles;
    private int operacionesDiasNoHabiles;
    private BigDecimal promedioVentasDiaHabil;
    private BigDecimal promedioVentasDiaNoHabil;
    private BigDecimal promedioOperacionesDiaHabil;
    private BigDecimal promedioOperacionesDiaNoHabil;
    private String fuenteFestivos;

    public static ReporteEstacionalidad calcular(
            int anio,
            int mes,
            List<ResumenVentasDiarias> resumenesMes,
            List<DiaFestivo> festivosAnio) {
        if (mes < 1 || mes > 12) {
            throw new ReglaNegocioException("El mes debe estar comprendido entre 1 y 12.");
        }
        if (anio < 2000 || anio > 2100) {
            throw new ReglaNegocioException("El año especificado no es válido.");
        }

        Set<LocalDate> fechasFestivas = (festivosAnio != null)
                ? festivosAnio.stream()
                        .map(DiaFestivo::getFecha)
                        .filter(f -> f != null)
                        .collect(Collectors.toSet())
                : Set.of();

        String fuente = (festivosAnio != null && !festivosAnio.isEmpty() && festivosAnio.getFirst().getFuente() != null)
                ? festivosAnio.getFirst().getFuente()
                : DiaFestivo.FUENTE_API_LIVE;

        Map<LocalDate, ResumenVentasDiarias> porFecha = new HashMap<>();
        if (resumenesMes != null) {
            for (ResumenVentasDiarias r : resumenesMes) {
                if (r.getFecha() != null) {
                    porFecha.put(r.getFecha(), r);
                }
            }
        }

        YearMonth yearMonth = YearMonth.of(anio, mes);
        int totalDiasHabiles = 0;
        int totalDiasNoHabiles = 0;
        BigDecimal totalVentasHabiles = BigDecimal.ZERO;
        BigDecimal totalVentasNoHabiles = BigDecimal.ZERO;
        int totalOpsHabiles = 0;
        int totalOpsNoHabiles = 0;

        for (int dia = 1; dia <= yearMonth.lengthOfMonth(); dia++) {
            LocalDate fechaDia = yearMonth.atDay(dia);
            boolean esHabil = fechaDia.getDayOfWeek() != DayOfWeek.SUNDAY && !fechasFestivas.contains(fechaDia);
            ResumenVentasDiarias resumen = porFecha.get(fechaDia);
            BigDecimal ventasNetas = resumen != null ? resumen.calcularVentasNetas() : BigDecimal.ZERO;
            int ops = (resumen != null && resumen.getCantidadOperaciones() != null) ? resumen.getCantidadOperaciones() : 0;

            if (esHabil) {
                totalDiasHabiles++;
                totalVentasHabiles = totalVentasHabiles.add(ventasNetas);
                totalOpsHabiles += ops;
            } else {
                totalDiasNoHabiles++;
                totalVentasNoHabiles = totalVentasNoHabiles.add(ventasNetas);
                totalOpsNoHabiles += ops;
            }
        }

        totalVentasHabiles = totalVentasHabiles.setScale(2, RoundingMode.HALF_EVEN);
        totalVentasNoHabiles = totalVentasNoHabiles.setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal promVentasHabil = totalDiasHabiles > 0
                ? totalVentasHabiles.divide(BigDecimal.valueOf(totalDiasHabiles), 2, RoundingMode.HALF_EVEN)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal promVentasNoHabil = totalDiasNoHabiles > 0
                ? totalVentasNoHabiles.divide(BigDecimal.valueOf(totalDiasNoHabiles), 2, RoundingMode.HALF_EVEN)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal promOpsHabil = totalDiasHabiles > 0
                ? BigDecimal.valueOf(totalOpsHabiles).divide(BigDecimal.valueOf(totalDiasHabiles), 2, RoundingMode.HALF_EVEN)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal promOpsNoHabil = totalDiasNoHabiles > 0
                ? BigDecimal.valueOf(totalOpsNoHabiles).divide(BigDecimal.valueOf(totalDiasNoHabiles), 2, RoundingMode.HALF_EVEN)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

        return ReporteEstacionalidad.builder()
                .anio(anio)
                .mes(mes)
                .diasHabiles(totalDiasHabiles)
                .diasNoHabiles(totalDiasNoHabiles)
                .ventasDiasHabiles(totalVentasHabiles)
                .ventasDiasNoHabiles(totalVentasNoHabiles)
                .operacionesDiasHabiles(totalOpsHabiles)
                .operacionesDiasNoHabiles(totalOpsNoHabiles)
                .promedioVentasDiaHabil(promVentasHabil)
                .promedioVentasDiaNoHabil(promVentasNoHabil)
                .promedioOperacionesDiaHabil(promOpsHabil)
                .promedioOperacionesDiaNoHabil(promOpsNoHabil)
                .fuenteFestivos(fuente)
                .build();
    }
}
