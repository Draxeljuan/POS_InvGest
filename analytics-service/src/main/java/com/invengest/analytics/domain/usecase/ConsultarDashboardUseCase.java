package com.invengest.analytics.domain.usecase;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.DashboardResumen;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.domain.model.gateway.AlertaStockGateway;
import com.invengest.analytics.domain.model.gateway.ResumenVentaGateway;
import com.invengest.analytics.domain.model.gateway.TopProductoGateway;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ConsultarDashboardUseCase {

    private static final int DIAS_HISTORIAL_POR_DEFECTO = 7;
    private static final int LIMITE_TOP_PRODUCTOS = 5;
    private static final int LIMITE_ALERTAS_RECIENTES = 10;

    private final ResumenVentaGateway resumenVentaGateway;
    private final TopProductoGateway topProductoGateway;
    private final AlertaStockGateway alertaStockGateway;

    public ConsultarDashboardUseCase(
            ResumenVentaGateway resumenVentaGateway,
            TopProductoGateway topProductoGateway,
            AlertaStockGateway alertaStockGateway) {
        this.resumenVentaGateway = resumenVentaGateway;
        this.topProductoGateway = topProductoGateway;
        this.alertaStockGateway = alertaStockGateway;
    }

    public DashboardResumen obtenerResumenDashboard(LocalDate fechaReferencia, Integer diasHistorial) {
        LocalDate fechaObjetivo = fechaReferencia != null ? fechaReferencia : LocalDate.now();
        int dias = (diasHistorial != null && diasHistorial > 0) ? diasHistorial : DIAS_HISTORIAL_POR_DEFECTO;

        LocalDate fechaInicioPeriodo = fechaObjetivo.minusDays(dias - 1L);

        Optional<ResumenVentasDiarias> resumenHoy = resumenVentaGateway.findByFecha(fechaObjetivo);
        BigDecimal ventasDelDia = resumenHoy
                .map(ResumenVentasDiarias::getTotalVentas)
                .filter(v -> v != null)
                .orElse(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
        BigDecimal ventasAnuladasDelDia = resumenHoy
                .map(ResumenVentasDiarias::getTotalAnulado)
                .filter(v -> v != null)
                .orElse(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
        BigDecimal ventasNetasDelDia = resumenHoy
                .map(ResumenVentasDiarias::calcularVentasNetas)
                .orElse(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
        Integer operacionesDelDia = resumenHoy
                .map(ResumenVentasDiarias::getCantidadOperaciones)
                .filter(o -> o != null)
                .orElse(0);

        List<ResumenVentasDiarias> historialVentas = resumenVentaGateway.findByFechaBetween(fechaInicioPeriodo, fechaObjetivo);

        BigDecimal ventasAcumuladasPeriodo = historialVentas.stream()
                .map(ResumenVentasDiarias::calcularVentasNetas)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_EVEN);

        List<TopProducto> topProductos = topProductoGateway.findTopNByFechaBetween(
                fechaInicioPeriodo, fechaObjetivo, LIMITE_TOP_PRODUCTOS);

        List<AlertaStock> alertasRecientes = alertaStockGateway.findRecentAlerts(LIMITE_ALERTAS_RECIENTES);

        return DashboardResumen.builder()
                .fechaConsulta(fechaObjetivo)
                .ventasDelDia(ventasDelDia)
                .ventasAnuladasDelDia(ventasAnuladasDelDia)
                .ventasNetasDelDia(ventasNetasDelDia)
                .operacionesDelDia(operacionesDelDia)
                .ventasAcumuladasPeriodo(ventasAcumuladasPeriodo)
                .historialVentasRecientes(historialVentas)
                .productosMasVendidos(topProductos)
                .alertasRecientes(alertasRecientes)
                .build();
    }

    public List<ResumenVentasDiarias> consultarVentasPorRango(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            throw new ReglaNegocioException("Las fechas de inicio y fin son obligatorias.");
        }
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        return resumenVentaGateway.findByFechaBetween(inicio, fin);
    }
}
