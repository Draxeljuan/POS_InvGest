package com.invengest.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RolUsuario {

    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final String ROL_VENDEDOR = "Vendedor";

    private Integer idRol;
    private String nombre;

    public boolean esAdministrador() {
        return ROL_ADMINISTRADOR.equalsIgnoreCase(this.nombre);
    }
}
