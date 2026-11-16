package com.invengest.transaction.domain.model.gateway;

import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.Venta;

public interface NotificacionGateway {
    void enviarComprobanteVenta(Venta venta, String destinatarioOverride);
    void enviarResumenCierreCaja(CierreCaja cierreCaja);
}
