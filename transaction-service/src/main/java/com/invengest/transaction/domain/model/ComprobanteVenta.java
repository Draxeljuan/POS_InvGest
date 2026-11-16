package com.invengest.transaction.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteVenta {
    private Integer idVenta;
    private String numeroComprobante;
    private LocalDateTime fecha;
    private String estado;
    private Integer idUsuario;
    private String nombreCliente;
    private String documentoCliente;
    private String emailCliente;
    private String metodoPago;
    private BigDecimal total;
    private List<DetalleVenta> detalles = new ArrayList<>();

    public static ComprobanteVenta desdeVenta(Venta venta) {
        String numero = venta.getIdVenta() != null
                ? String.format("POS-%06d", venta.getIdVenta())
                : "POS-000000";
        String nombreCliente = venta.getCliente() != null && !venta.getCliente().getNombreCompleto().isBlank()
                ? venta.getCliente().getNombreCompleto()
                : "Consumidor Final";
        String documentoCliente = venta.getCliente() != null ? venta.getCliente().getDocumentoIdentidad() : null;
        String emailCliente = venta.getCliente() != null ? venta.getCliente().getEmail() : null;
        String nombreMetodo = venta.getMetodoPago() != null ? venta.getMetodoPago().getNombre() : "Sin especificar";

        return new ComprobanteVenta(
                venta.getIdVenta(),
                numero,
                venta.getFecha(),
                venta.getEstado(),
                venta.getIdUsuario(),
                nombreCliente,
                documentoCliente,
                emailCliente,
                nombreMetodo,
                venta.getTotal(),
                venta.getDetalles() != null ? venta.getDetalles() : List.of()
        );
    }
}
