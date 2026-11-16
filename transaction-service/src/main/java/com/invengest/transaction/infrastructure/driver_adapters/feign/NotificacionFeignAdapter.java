package com.invengest.transaction.infrastructure.driver_adapters.feign;

import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.NotificacionGateway;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ContactoUsuarioFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ItemComprobanteFeignDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.NotificacionCierreCajaFeignRequestDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.NotificacionComprobanteVentaFeignRequestDTO;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.TotalMetodoPagoFeignDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificacionFeignAdapter implements NotificacionGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificacionFeignAdapter.class);

    private final NotificationFeignClient notificationFeignClient;
    private final AuthFeignClient authFeignClient;

    @Override
    public void enviarComprobanteVenta(Venta venta, String destinatarioOverride) {
        if (venta == null) {
            return;
        }
        String destinatario = destinatarioOverride != null && !destinatarioOverride.isBlank()
                ? destinatarioOverride.trim()
                : (venta.getCliente() != null ? venta.getCliente().getEmail() : null);

        if (destinatario == null || destinatario.isBlank() || !destinatario.contains("@")) {
            log.warn("notificacion_comprobante_omitida motivo=sin_destinatario_valido idVenta={}", venta.getIdVenta());
            return;
        }

        String nombreCliente = venta.getCliente() != null && !venta.getCliente().getNombreCompleto().isBlank()
                ? venta.getCliente().getNombreCompleto()
                : "Consumidor Final";
        String metodoPago = venta.getMetodoPago() != null ? venta.getMetodoPago().getNombre() : "Efectivo";
        LocalDateTime fecha = venta.getFecha() != null ? venta.getFecha() : LocalDateTime.now();

        List<ItemComprobanteFeignDTO> items = venta.getDetalles() != null
                ? venta.getDetalles().stream()
                        .map(d -> new ItemComprobanteFeignDTO(
                                d.getNombreItem(),
                                d.getCantidad(),
                                d.getPrecioUnitario(),
                                d.getSubtotal()
                        ))
                        .toList()
                : List.of();

        try {
            notificationFeignClient.enviarComprobanteVenta(new NotificacionComprobanteVentaFeignRequestDTO(
                    destinatario,
                    nombreCliente,
                    venta.getIdVenta(),
                    fecha,
                    items,
                    venta.getTotal(),
                    metodoPago
            ));
        } catch (RuntimeException ex) {
            log.warn("notificacion_fallida tipo=COMPROBANTE_VENTA idVenta={} destinatario={} causa={}",
                    venta.getIdVenta(), destinatario, ex.getMessage());
        }
    }

    @Override
    public void enviarResumenCierreCaja(CierreCaja cierreCaja) {
        if (cierreCaja == null) {
            return;
        }
        try {
            List<ContactoUsuarioFeignDTO> contactos = authFeignClient.obtenerContactosAdministradores();
            if (contactos == null || contactos.isEmpty()) {
                log.warn("notificacion_cierre_caja_omitida motivo=sin_administradores_activos fecha={}",
                        cierreCaja.getFechaCierre());
                return;
            }

            List<TotalMetodoPagoFeignDTO> totalesPorMetodo = cierreCaja.getDetalles() != null
                    ? cierreCaja.getDetalles().stream()
                            .map(d -> new TotalMetodoPagoFeignDTO(
                                    d.getMetodoPago() != null ? d.getMetodoPago().getNombre() : "Otro",
                                    d.getTotalMetodo(),
                                    d.getCantidadOperaciones()
                            ))
                            .toList()
                    : List.of();

            for (ContactoUsuarioFeignDTO contacto : contactos) {
                if (contacto != null && contacto.email() != null && !contacto.email().isBlank()) {
                    notificationFeignClient.enviarResumenCierreCaja(new NotificacionCierreCajaFeignRequestDTO(
                            contacto.email().trim(),
                            cierreCaja.getFechaCierre(),
                            totalesPorMetodo,
                            cierreCaja.getEfectivoEsperado(),
                            cierreCaja.getEfectivoContado(),
                            cierreCaja.getDiferencia(),
                            cierreCaja.getEsDiaHabil(),
                            cierreCaja.getProximoDiaHabil()
                    ));
                }
            }
        } catch (RuntimeException ex) {
            log.warn("notificacion_fallida tipo=CIERRE_CAJA fecha={} causa={}",
                    cierreCaja.getFechaCierre(), ex.getMessage());
        }
    }
}
