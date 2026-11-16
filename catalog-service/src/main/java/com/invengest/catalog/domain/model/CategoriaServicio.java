package com.invengest.catalog.domain.model;

import com.invengest.catalog.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaServicio {

    public static final String CATEGORIA_DESHABILITADOS = "Deshabilitados";

    private Integer idCategoriaServicio;
    private String nombre;

    public void validarInvariantes() {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre de la categoría de servicio es obligatorio.");
        }
        this.nombre = nombre.trim();
    }

    public boolean esDeshabilitado() {
        return nombre != null && CATEGORIA_DESHABILITADOS.equalsIgnoreCase(nombre.trim());
    }
}
