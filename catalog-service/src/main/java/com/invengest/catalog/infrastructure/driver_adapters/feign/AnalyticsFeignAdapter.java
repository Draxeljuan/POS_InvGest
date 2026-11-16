package com.invengest.catalog.infrastructure.driver_adapters.feign;

import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.gateway.AnalyticsGateway;
import com.invengest.catalog.infrastructure.driver_adapters.feign.dto.SincronizarAlertaStockRequestDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyticsFeignAdapter implements AnalyticsGateway {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsFeignAdapter.class);

    private final AnalyticsFeignClient analyticsFeignClient;

    @Override
    public void notificarEstadoStock(Producto producto) {
        if (producto == null) {
            return;
        }
        Short stockActual = producto.getStock() != null ? producto.getStock().shortValue() : 0;
        Short stockMinimo = producto.getStockMinimo() != null ? producto.getStockMinimo().shortValue() : 0;
        String estadoAlerta = producto.obtenerEtiquetaAlertaStock();

        try {
            analyticsFeignClient.sincronizarAlertaStock(new SincronizarAlertaStockRequestDTO(
                    producto.getIdProducto(),
                    producto.getNombre(),
                    stockActual,
                    stockMinimo,
                    estadoAlerta
            ));
        } catch (RuntimeException ex) {
            log.warn("analytics_sync_fallida tipo=ALERTA_STOCK idProducto={} stockActual={} stockMinimo={} causa={}",
                    producto.getIdProducto(), stockActual, stockMinimo, ex.getMessage());
        }
    }
}
