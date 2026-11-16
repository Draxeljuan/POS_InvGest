package com.invengest.catalog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoProducto {

    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_DESCONTINUADO = "Descontinuado";
    public static final String ESTADO_AGOTADO = "Agotado";

    private Integer idEstado;
    private String nombre;

    public boolean esActivo() {
        return nombre != null && ESTADO_ACTIVO.equalsIgnoreCase(nombre.trim());
    }

    public boolean esAgotado() {
        return nombre != null && ESTADO_AGOTADO.equalsIgnoreCase(nombre.trim());
    }

    public boolean esDescontinuado() {
        return nombre != null && ESTADO_DESCONTINUADO.equalsIgnoreCase(nombre.trim());
    }
}
