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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenVentasDiarias {

    private LocalDate fecha;
    private BigDecimal totalVentas;
    private BigDecimal totalAnulado;
    private Integer cantidadOperaciones;

    public static ResumenVentasDiarias crearVacio(LocalDate fecha) {
        return ResumenVentasDiarias.builder()
                .fecha(fecha != null ? fecha : LocalDate.now())
                .totalVentas(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                .totalAnulado(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                .cantidadOperaciones(0)
                .build();
    }

    public void registrarVenta(BigDecimal montoVenta) {
        if (montoVenta == null || montoVenta.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El monto total de la venta debe ser mayor o igual a cero.");
        }
        BigDecimal baseVentas = totalVentas != null ? totalVentas : BigDecimal.ZERO;
        BigDecimal baseAnulado = totalAnulado != null ? totalAnulado : BigDecimal.ZERO;
        int baseOps = cantidadOperaciones != null ? cantidadOperaciones : 0;

        this.totalVentas = baseVentas.add(montoVenta).setScale(2, RoundingMode.HALF_EVEN);
        this.totalAnulado = baseAnulado.setScale(2, RoundingMode.HALF_EVEN);
        this.cantidadOperaciones = baseOps + 1;
    }

    public void registrarAnulacion(BigDecimal montoAnulado) {
        if (montoAnulado == null || montoAnulado.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El monto anulado debe ser mayor o igual a cero.");
        }
        BigDecimal baseVentas = totalVentas != null ? totalVentas : BigDecimal.ZERO;
        BigDecimal baseAnulado = totalAnulado != null ? totalAnulado : BigDecimal.ZERO;
        int baseOps = cantidadOperaciones != null ? cantidadOperaciones : 0;

        this.totalAnulado = baseAnulado.add(montoAnulado).setScale(2, RoundingMode.HALF_EVEN);
        this.totalVentas = baseVentas.setScale(2, RoundingMode.HALF_EVEN);
        this.cantidadOperaciones = Math.max(0, baseOps - 1);
    }

    public BigDecimal calcularVentasNetas() {
        BigDecimal brutas = totalVentas != null ? totalVentas : BigDecimal.ZERO;
        BigDecimal anuladas = totalAnulado != null ? totalAnulado : BigDecimal.ZERO;
        BigDecimal netas = brutas.subtract(anuladas);
        return (netas.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : netas)
                .setScale(2, RoundingMode.HALF_EVEN);
    }
}
