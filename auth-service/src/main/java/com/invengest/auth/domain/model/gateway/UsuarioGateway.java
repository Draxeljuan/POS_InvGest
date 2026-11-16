package com.invengest.auth.domain.model.gateway;

import com.invengest.auth.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioGateway {
    Optional<Usuario> findById(Integer idUsuario);
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findAll();
    List<Usuario> findActiveByRolNombre(String nombreRol);
    Usuario save(Usuario usuario);
}
