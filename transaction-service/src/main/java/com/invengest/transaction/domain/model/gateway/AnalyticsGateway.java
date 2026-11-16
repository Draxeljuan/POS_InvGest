package com.invengest.transaction.domain.model.gateway;

import com.invengest.transaction.domain.model.Venta;

public interface AnalyticsGateway {
    void sincronizarVenta(Venta venta);
    void sincronizarAnulacionVenta(Venta venta);
}
