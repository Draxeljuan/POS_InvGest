package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;

public record SolicitudItemStock(
        String idProducto,
        Integer cantidad
) {
    public SolicitudItemStock {
        if (idProducto == null || idProducto.isBlank()) {
            throw new ReglaNegocioException("El código del producto es obligatorio en la solicitud de stock.");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad solicitada debe ser mayor a cero para el producto: " + idProducto);
        }
        idProducto = idProducto.trim();
    }
}
