package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.ComprobanteVenta;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.NotificacionGateway;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ComprobanteVentaUseCase {

    private final VentaGateway ventaGateway;
    private final NotificacionGateway notificacionGateway;

    public ComprobanteVenta obtenerComprobante(Integer idVenta) {
        if (idVenta == null) {
            throw new ReglaNegocioException("El ID de la venta es obligatorio");
        }
        Venta venta = ventaGateway.findById(idVenta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con ID: " + idVenta));
        return ComprobanteVenta.desdeVenta(venta);
    }

    public ComprobanteVenta enviarComprobantePorCorreo(Integer idVenta, String emailDestino) {
        if (idVenta == null) {
            throw new ReglaNegocioException("El ID de la venta es obligatorio");
        }
        Venta venta = ventaGateway.findById(idVenta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con ID: " + idVenta));

        String destinatario = emailDestino != null && !emailDestino.isBlank()
                ? emailDestino.trim()
                : (venta.getCliente() != null ? venta.getCliente().getEmail() : null);

        if (destinatario == null || destinatario.isBlank() || !destinatario.contains("@")) {
            throw new ReglaNegocioException(
                    "No se encontró un correo electrónico válido para enviar el comprobante de la venta #" + idVenta);
        }

        notificacionGateway.enviarComprobanteVenta(venta, destinatario);

        ComprobanteVenta comprobante = ComprobanteVenta.desdeVenta(venta);
        comprobante.setEmailCliente(destinatario);
        return comprobante;
    }
}
