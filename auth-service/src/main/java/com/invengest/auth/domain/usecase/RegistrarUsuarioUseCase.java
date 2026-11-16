package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.exception.ReglaNegocioException;
import com.invengest.auth.domain.exception.UsuarioYaExisteException;
import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.NotificacionGateway;
import com.invengest.auth.domain.model.gateway.PasswordEncoderGateway;
import com.invengest.auth.domain.model.gateway.RolUsuarioGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegistrarUsuarioUseCase {

    private static final int LONGITUD_MINIMA_CONTRASENA = 8;

    private final UsuarioGateway usuarioGateway;
    private final RolUsuarioGateway rolUsuarioGateway;
    private final PasswordEncoderGateway passwordEncoder;
    private final NotificacionGateway notificacionGateway;

    public Usuario execute(Usuario usuario, String rawPassword) {
        if (rawPassword == null || rawPassword.trim().length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new ReglaNegocioException("La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres");
        }

        if (usuarioGateway.findByNombreUsuario(usuario.getNombreUsuario()).isPresent()) {
            throw new UsuarioYaExisteException("El nombre de usuario ya está registrado");
        }

        if (usuarioGateway.findByEmail(usuario.getEmail()).isPresent()) {
            throw new UsuarioYaExisteException("El correo electrónico ya está registrado");
        }

        Integer idRol = usuario.getRol() != null ? usuario.getRol().getIdRol() : null;
        if (idRol == null) {
            throw new ReglaNegocioException("El rol del usuario es obligatorio");
        }

        RolUsuario rolExistente = rolUsuarioGateway.findById(idRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("El rol con ID " + idRol + " no existe"));

        usuario.setRol(rolExistente);
        usuario.setContrasena(passwordEncoder.encode(rawPassword));
        usuario.activar();

        Usuario usuarioGuardado = usuarioGateway.save(usuario);

        notificacionGateway.enviarCorreoBienvenida(
                usuarioGuardado.getEmail(),
                usuarioGuardado.obtenerNombreCompleto(),
                usuarioGuardado.getNombreUsuario(),
                rolExistente.getNombre()
        );

        return usuarioGuardado;
    }
}
