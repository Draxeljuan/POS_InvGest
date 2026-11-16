package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.exception.UsuarioYaExisteException;
import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.RolUsuarioGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ActualizarUsuarioUseCase {

    private final UsuarioGateway usuarioGateway;
    private final RolUsuarioGateway rolUsuarioGateway;

    public Usuario execute(Integer idUsuario, Usuario datosActualizados) {
        Usuario existente = usuarioGateway.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + idUsuario));

        if (datosActualizados.getEmail() != null
                && !datosActualizados.getEmail().equalsIgnoreCase(existente.getEmail())) {
            usuarioGateway.findByEmail(datosActualizados.getEmail())
                    .filter(u -> !u.getIdUsuario().equals(idUsuario))
                    .ifPresent(u -> {
                        throw new UsuarioYaExisteException("El correo electrónico ya está registrado por otro usuario");
                    });
            existente.setEmail(datosActualizados.getEmail());
        }

        if (datosActualizados.getNombre() != null && !datosActualizados.getNombre().isBlank()) {
            existente.setNombre(datosActualizados.getNombre());
        }

        if (datosActualizados.getApellido() != null && !datosActualizados.getApellido().isBlank()) {
            existente.setApellido(datosActualizados.getApellido());
        }

        if (datosActualizados.getTelefono() != null) {
            existente.setTelefono(datosActualizados.getTelefono());
        }

        if (datosActualizados.getRol() != null && datosActualizados.getRol().getIdRol() != null) {
            Integer nuevoIdRol = datosActualizados.getRol().getIdRol();
            RolUsuario nuevoRol = rolUsuarioGateway.findById(nuevoIdRol)
                    .orElseThrow(() -> new RecursoNoEncontradoException("El rol con ID " + nuevoIdRol + " no existe"));
            existente.setRol(nuevoRol);
        }

        return usuarioGateway.save(existente);
    }
}
