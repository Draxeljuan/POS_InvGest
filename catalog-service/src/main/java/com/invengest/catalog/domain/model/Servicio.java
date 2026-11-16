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
public class Servicio {

    private Integer idServicio;
    private CategoriaServicio categoriaServicio;
    private String nombre;
    private String descripcion;
    private BigDecimal precioSugerido;

    public void validarInvariantes() {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre del servicio es obligatorio.");
        }
        if (precioSugerido == null || precioSugerido.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El precio sugerido del servicio no puede ser negativo.");
        }
        this.nombre = nombre.trim();
    }

    public void deshabilitar(CategoriaServicio categoriaDeshabilitados) {
        if (categoriaDeshabilitados == null) {
            throw new ReglaNegocioException("La categoría 'Deshabilitados' es requerida para deshabilitar el servicio.");
        }
        this.categoriaServicio = categoriaDeshabilitados;
    }
}
