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
public class Proveedor {

    private Integer idProveedor;
    private EstadoProveedor estado;
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;
    private String nit;

    public void validarInvariantes() {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre del proveedor es obligatorio.");
        }
        this.nombre = nombre.trim();
        if (nit != null && !nit.isBlank()) {
            this.nit = nit.trim();
        }
    }

    public boolean estaActivo() {
        return estado != null && estado.esActivo();
    }

    public void desactivar(EstadoProveedor estadoInactivo) {
        if (estadoInactivo == null) {
            throw new ReglaNegocioException("El estado 'Inactivo' es requerido para dar de baja al proveedor.");
        }
        this.estado = estadoInactivo;
    }
}
