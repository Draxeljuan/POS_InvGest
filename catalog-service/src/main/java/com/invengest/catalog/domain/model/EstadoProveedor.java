package com.invengest.catalog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoProveedor {

    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_INACTIVO = "Inactivo";

    private Integer idEstadoProveedor;
    private String nombre;

    public boolean esActivo() {
        return nombre != null && ESTADO_ACTIVO.equalsIgnoreCase(nombre.trim());
    }

    public boolean esInactivo() {
        return nombre != null && ESTADO_INACTIVO.equalsIgnoreCase(nombre.trim());
    }
}
