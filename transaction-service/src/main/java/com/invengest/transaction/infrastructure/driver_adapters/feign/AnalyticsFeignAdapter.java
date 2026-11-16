package com.invengest.transaction.infrastructure.driver_adapters.feign;

import com.invengest.transaction.domain.model.DetalleVenta;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.AnalyticsGateway;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ItemVentaAnalyticsFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.SincronizarAnulacionVentaFeignRequestDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.SincronizarVentaFeignRequestDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalyticsFeignAdapter implements AnalyticsGateway {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsFeignAdapter.class);

    private final AnalyticsFeignClient analyticsFeignClient;

    @Override
    public void sincronizarVenta(Venta venta) {
        if (venta == null) {
            return;
        }
        LocalDate fecha = venta.getFecha() != null ? venta.getFecha().toLocalDate() : LocalDate.now();
        List<ItemVentaAnalyticsFeignDTO> items = mapearItemsProducto(venta.obtenerDetallesProducto());

        try {
            analyticsFeignClient.sincronizarVenta(new SincronizarVentaFeignRequestDTO(
                    fecha,
                    venta.getTotal(),
                    items
            ));
        } catch (RuntimeException ex) {
            log.warn("analytics_sync_fallida tipo=VENTA idVenta={} total={} causa={}",
                    venta.getIdVenta(), venta.getTotal(), ex.getMessage());
        }
    }

    @Override
    public void sincronizarAnulacionVenta(Venta venta) {
        if (venta == null) {
            return;
        }
        LocalDate fechaVenta = venta.getFecha() != null ? venta.getFecha().toLocalDate() : LocalDate.now();
        List<ItemVentaAnalyticsFeignDTO> items = mapearItemsProducto(venta.obtenerDetallesProducto());

        try {
            analyticsFeignClient.sincronizarAnulacionVenta(new SincronizarAnulacionVentaFeignRequestDTO(
                    venta.getIdVenta(),
                    fechaVenta,
                    venta.getTotal(),
                    venta.getMotivoAnulacion(),
                    items
            ));
        } catch (RuntimeException ex) {
            log.warn("analytics_sync_fallida tipo=ANULACION_VENTA idVenta={} totalAnulado={} causa={}",
                    venta.getIdVenta(), venta.getTotal(), ex.getMessage());
        }
    }

    private List<ItemVentaAnalyticsFeignDTO> mapearItemsProducto(List<DetalleVenta> detallesProducto) {
        if (detallesProducto == null || detallesProducto.isEmpty()) {
            return List.of();
        }
        return detallesProducto.stream()
                .map(d -> new ItemVentaAnalyticsFeignDTO(
                        d.getIdItem(),
                        d.getNombreItem(),
                        d.getCantidad()
                ))
                .toList();
    }
}
