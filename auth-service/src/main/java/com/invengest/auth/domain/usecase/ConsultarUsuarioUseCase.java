package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.RolUsuarioGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ConsultarUsuarioUseCase {

    private final UsuarioGateway usuarioGateway;
    private final RolUsuarioGateway rolUsuarioGateway;

    public Usuario obtenerPorId(Integer idUsuario) {
        return usuarioGateway.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + idUsuario));
    }

    public Usuario obtenerPorUsername(String username) {
        return usuarioGateway.findByNombreUsuario(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));
    }

    public List<Usuario> listarUsuarios() {
        return usuarioGateway.findAll();
    }

    public List<Usuario> listarAdministradoresActivos() {
        return usuarioGateway.findActiveByRolNombre(RolUsuario.ROL_ADMINISTRADOR);
    }

    public List<RolUsuario> listarRoles() {
        return rolUsuarioGateway.findAll();
    }
}
