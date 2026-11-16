package com.invengest.auth.domain.model;

import com.invengest.auth.domain.exception.ReglaNegocioException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Usuario {

    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private LocalDateTime ultimoAcceso;
    private RolUsuario rol;
    private String nombreUsuario;
    private String contrasena;
    private Boolean activo;

    public boolean estaActivo() {
        return Boolean.TRUE.equals(this.activo);
    }

    public boolean esAdministrador() {
        return this.rol != null && this.rol.esAdministrador();
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        if (esAdministrador()) {
            throw new ReglaNegocioException("No se puede desactivar una cuenta con rol Administrador");
        }
        this.activo = false;
    }

    public void registrarUltimoAcceso(LocalDateTime fechaHora) {
        this.ultimoAcceso = fechaHora;
    }

    public String obtenerNombreCompleto() {
        String n = this.nombre != null ? this.nombre.trim() : "";
        String a = this.apellido != null ? this.apellido.trim() : "";
        return (n + " " + a).trim();
    }
}
