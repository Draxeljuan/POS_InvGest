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
public class Categoria {

    private Integer idCategoria;
    private String nombre;
    private String descripcion;

    public void validarInvariantes() {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre de la categoría es obligatorio.");
        }
        this.nombre = nombre.trim();
        if (descripcion != null) {
            this.descripcion = descripcion.trim();
        }
    }
}
