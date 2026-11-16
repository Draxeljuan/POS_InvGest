package com.invengest.transaction.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionStockItem {
    private String idProducto;
    private String nombre;
    private BigDecimal precioVenta;
    private Integer stockDisponible;
    private Integer cantidadSolicitada;
    private boolean suficiente;
}
