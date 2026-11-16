package com.invengest.catalog.domain.model.gateway;

import com.invengest.catalog.domain.model.Producto;

import java.util.List;

public interface NotificacionStockGateway {
    void enviarAlertaStock(List<Producto> productosEnAlerta);
}
