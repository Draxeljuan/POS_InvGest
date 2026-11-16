package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.CredencialesInvalidasException;
import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.exception.ReglaNegocioException;
import com.invengest.auth.domain.model.EventoSeguridadCuenta;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.NotificacionGateway;
import com.invengest.auth.domain.model.gateway.PasswordEncoderGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;

@RequiredArgsConstructor
public class GestionarContrasenaUseCase {

    private static final int LONGITUD_MINIMA = 8;
    private static final String ALFABETO_TEMPORAL = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioGateway usuarioGateway;
    private final PasswordEncoderGateway passwordEncoder;
    private final NotificacionGateway notificacionGateway;

    public void cambiarContrasenaPropia(String username, String contrasenaActual, String nuevaContrasena) {
        Usuario usuario = usuarioGateway.findByNombreUsuario(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasena())) {
            throw new CredencialesInvalidasException("La contraseña actual es incorrecta");
        }

        if (nuevaContrasena == null || nuevaContrasena.trim().length() < LONGITUD_MINIMA) {
            throw new ReglaNegocioException("La nueva contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres");
        }

        if (nuevaContrasena.equals(contrasenaActual)) {
            throw new ReglaNegocioException("La nueva contraseña debe ser diferente a la contraseña actual");
        }

        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioGateway.save(usuario);

        notificacionGateway.enviarCorreoSeguridadCuenta(
                usuario.getEmail(),
                usuario.obtenerNombreCompleto(),
                EventoSeguridadCuenta.CAMBIO_CONTRASENA,
                null
        );
    }

    public String restablecerContrasenaPorAdmin(Integer idUsuarioObjetivo) {
        Usuario objetivo = usuarioGateway.findById(idUsuarioObjetivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + idUsuarioObjetivo));

        String contrasenaTemporal = generarContrasenaTemporal();
        objetivo.setContrasena(passwordEncoder.encode(contrasenaTemporal));
        usuarioGateway.save(objetivo);

        notificacionGateway.enviarCorreoSeguridadCuenta(
                objetivo.getEmail(),
                objetivo.obtenerNombreCompleto(),
                EventoSeguridadCuenta.RESTABLECIMIENTO,
                contrasenaTemporal
        );

        return contrasenaTemporal;
    }

    private String generarContrasenaTemporal() {
        StringBuilder sb = new StringBuilder("Tmp#");
        for (int i = 0; i < 8; i++) {
            int idx = SECURE_RANDOM.nextInt(ALFABETO_TEMPORAL.length());
            sb.append(ALFABETO_TEMPORAL.charAt(idx));
        }
        return sb.toString();
    }
}
