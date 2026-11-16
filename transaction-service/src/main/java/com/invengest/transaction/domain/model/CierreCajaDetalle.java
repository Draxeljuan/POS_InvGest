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
public class CierreCajaDetalle {
    private Integer idDetalle;
    private Integer idCierre;
    private MetodoPago metodoPago;
    private BigDecimal totalMetodo;
    private Integer cantidadOperaciones;
}
