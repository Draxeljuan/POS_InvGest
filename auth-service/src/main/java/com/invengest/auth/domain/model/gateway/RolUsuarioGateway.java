package com.invengest.auth.domain.model.gateway;

import com.invengest.auth.domain.model.RolUsuario;

import java.util.List;
import java.util.Optional;

public interface RolUsuarioGateway {
    Optional<RolUsuario> findById(Integer idRol);
    Optional<RolUsuario> findByNombre(String nombre);
    List<RolUsuario> findAll();
}
