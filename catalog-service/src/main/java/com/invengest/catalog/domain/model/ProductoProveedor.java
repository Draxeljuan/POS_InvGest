package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoProveedor {

    private String idProducto;
    private Integer idProveedor;
    private BigDecimal precioCompraProveedor;

    public void validarInvariantes() {
        if (idProducto == null || idProducto.isBlank()) {
            throw new ReglaNegocioException("El código del producto es obligatorio.");
        }
        if (idProveedor == null) {
            throw new ReglaNegocioException("El identificador del proveedor es obligatorio.");
        }
        if (precioCompraProveedor == null || precioCompraProveedor.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El precio de compra del proveedor no puede ser negativo.");
        }
        this.idProducto = idProducto.trim();
    }
}
