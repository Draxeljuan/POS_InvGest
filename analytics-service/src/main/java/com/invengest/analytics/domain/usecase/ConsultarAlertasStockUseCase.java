package com.invengest.analytics.domain.usecase;

import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.gateway.AlertaStockGateway;

import java.util.List;

public class ConsultarAlertasStockUseCase {

    private final AlertaStockGateway alertaStockGateway;

    public ConsultarAlertasStockUseCase(AlertaStockGateway alertaStockGateway) {
        this.alertaStockGateway = alertaStockGateway;
    }

    public List<AlertaStock> obtenerTodas() {
        return alertaStockGateway.findAll();
    }

    public List<AlertaStock> obtenerPorEstado(String estado) {
        String estadoNormalizado = AlertaStock.normalizarEstadoFiltro(estado);
        return alertaStockGateway.findByEstado(estadoNormalizado);
    }

    public List<AlertaStock> obtenerRecientes(Integer limite) {
        int limit = (limite != null && limite > 0) ? limite : 10;
        return alertaStockGateway.findRecentAlerts(limit);
    }
}
