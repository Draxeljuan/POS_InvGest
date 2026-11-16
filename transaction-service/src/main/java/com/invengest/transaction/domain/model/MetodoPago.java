package com.invengest.transaction.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPago {

    public static final String METODO_EFECTIVO = "Efectivo";

    private Integer idMetodoPago;
    private String nombre;

    public boolean esEfectivo() {
        return nombre != null && METODO_EFECTIVO.equalsIgnoreCase(nombre.trim());
    }
}
