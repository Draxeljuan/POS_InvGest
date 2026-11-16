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
public class ResumenMetodoPago {
    private Integer idMetodoPago;
    private String nombreMetodoPago;
    private BigDecimal total;
    private Integer cantidadOperaciones;
}
