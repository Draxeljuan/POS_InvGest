package com.invengest.analytics.domain.model;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenVentasUsd {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal totalVendidoCop;
    private BigDecimal totalAnuladoCop;
    private BigDecimal totalNetoCop;
    private BigDecimal totalVendidoUsd;
    private BigDecimal totalAnuladoUsd;
    private BigDecimal totalNetoUsd;
    private Integer cantidadOperaciones;
    private BigDecimal tasaCambioTrm;
    private LocalDate fechaVigenciaTrm;
    private String fuenteTrm;

    public static ResumenVentasUsd calcularDesdeResumenes(
            LocalDate inicio,
            LocalDate fin,
            List<ResumenVentasDiarias> resumenes,
            TasaCambio tasaCambio) {
        if (inicio == null || fin == null) {
            throw new ReglaNegocioException("Las fechas de inicio y fin son obligatorias.");
        }
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        if (tasaCambio == null) {
            throw new ReglaNegocioException("La tasa de cambio TRM es obligatoria para calcular el resumen en USD.");
        }

        BigDecimal vendidoCop = BigDecimal.ZERO;
        BigDecimal anuladoCop = BigDecimal.ZERO;
        int operaciones = 0;

        if (resumenes != null) {
            for (ResumenVentasDiarias r : resumenes) {
                if (r.getTotalVentas() != null) {
                    vendidoCop = vendidoCop.add(r.getTotalVentas());
                }
                if (r.getTotalAnulado() != null) {
                    anuladoCop = anuladoCop.add(r.getTotalAnulado());
                }
                if (r.getCantidadOperaciones() != null) {
                    operaciones += r.getCantidadOperaciones();
                }
            }
        }

        vendidoCop = vendidoCop.setScale(2, RoundingMode.HALF_EVEN);
        anuladoCop = anuladoCop.setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal netoCop = vendidoCop.subtract(anuladoCop);
        if (netoCop.compareTo(BigDecimal.ZERO) < 0) {
            netoCop = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        } else {
            netoCop = netoCop.setScale(2, RoundingMode.HALF_EVEN);
        }

        return ResumenVentasUsd.builder()
                .fechaInicio(inicio)
                .fechaFin(fin)
                .totalVendidoCop(vendidoCop)
                .totalAnuladoCop(anuladoCop)
                .totalNetoCop(netoCop)
                .totalVendidoUsd(tasaCambio.convertirAUsd(vendidoCop))
                .totalAnuladoUsd(tasaCambio.convertirAUsd(anuladoCop))
                .totalNetoUsd(tasaCambio.convertirAUsd(netoCop))
                .cantidadOperaciones(operaciones)
                .tasaCambioTrm(tasaCambio.valorCopPorUsd())
                .fechaVigenciaTrm(tasaCambio.fechaVigencia())
                .fuenteTrm(tasaCambio.fuente())
                .build();
    }
}
