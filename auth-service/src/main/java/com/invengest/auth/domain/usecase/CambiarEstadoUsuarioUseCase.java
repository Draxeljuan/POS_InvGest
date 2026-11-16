package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.exception.ReglaNegocioException;
import com.invengest.auth.domain.model.EventoSeguridadCuenta;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.NotificacionGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CambiarEstadoUsuarioUseCase {

    private final UsuarioGateway usuarioGateway;
    private final NotificacionGateway notificacionGateway;

    public Usuario execute(Integer idUsuarioObjetivo, boolean nuevoEstado, String usernameSolicitante) {
        Usuario objetivo = usuarioGateway.findById(idUsuarioObjetivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + idUsuarioObjetivo));

        if (!nuevoEstado) {
            if (usernameSolicitante != null && usernameSolicitante.equalsIgnoreCase(objetivo.getNombreUsuario())) {
                throw new ReglaNegocioException("Un usuario no puede desactivar su propia cuenta");
            }
            objetivo.desactivar();
            Usuario guardado = usuarioGateway.save(objetivo);
            notificacionGateway.enviarCorreoSeguridadCuenta(
                    guardado.getEmail(),
                    guardado.obtenerNombreCompleto(),
                    EventoSeguridadCuenta.DESACTIVACION,
                    null
            );
            return guardado;
        }

        objetivo.activar();
        return usuarioGateway.save(objetivo);
    }
}
