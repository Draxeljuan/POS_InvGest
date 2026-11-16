package com.invengest.catalog.infrastructure.driver_adapters.feign;

import com.invengest.catalog.domain.model.Producto;
import com.invengest.catalog.domain.model.gateway.NotificacionStockGateway;
import com.invengest.catalog.infrastructure.driver_adapters.feign.dto.ContactoUsuarioDTO;
import com.invengest.catalog.infrastructure.driver_adapters.feign.dto.ItemAlertaStockFeignDTO;
import com.invengest.catalog.infrastructure.driver_adapters.feign.dto.NotificacionAlertaStockFeignRequestDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificacionStockFeignAdapter implements NotificacionStockGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificacionStockFeignAdapter.class);

    private final AuthFeignClient authFeignClient;
    private final NotificationFeignClient notificationFeignClient;

    @Override
    public void enviarAlertaStock(List<Producto> productosEnAlerta) {
        if (productosEnAlerta == null || productosEnAlerta.isEmpty()) {
            return;
        }
        try {
            List<ContactoUsuarioDTO> contactos = authFeignClient.obtenerContactosAdministradores();
            if (contactos == null || contactos.isEmpty()) {
                log.warn("notificacion_alerta_stock_omitida motivo=sin_administradores_activos cantidadProductos={}",
                        productosEnAlerta.size());
                return;
            }

            List<String> destinatarios = contactos.stream()
                    .map(ContactoUsuarioDTO::email)
                    .filter(email -> email != null && !email.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();

            if (destinatarios.isEmpty()) {
                log.warn("notificacion_alerta_stock_omitida motivo=sin_correos_validos cantidadProductos={}",
                        productosEnAlerta.size());
                return;
            }

            List<ItemAlertaStockFeignDTO> items = productosEnAlerta.stream()
                    .map(p -> new ItemAlertaStockFeignDTO(
                            p.getIdProducto(),
                            p.getNombre(),
                            p.getStock() != null ? p.getStock() : 0,
                            p.getStockMinimo() != null ? p.getStockMinimo() : 0,
                            p.obtenerEtiquetaAlertaStock()
                    ))
                    .toList();

            notificationFeignClient.enviarAlertaStock(new NotificacionAlertaStockFeignRequestDTO(destinatarios, items));
        } catch (RuntimeException ex) {
            log.warn("notificacion_alerta_stock_fallida cantidadProductos={} causa={}",
                    productosEnAlerta.size(), ex.getMessage());
        }
    }
}
