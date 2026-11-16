package com.invengest.auth.infrastructure.mapper;

import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.entity.RolUsuarioData;
import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.entity.UsuarioData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioEntityMapper {

    Usuario toUsuario(UsuarioData usuarioData);

    UsuarioData toUsuarioData(Usuario usuario);

    RolUsuario toRolUsuario(RolUsuarioData rolUsuarioData);

    RolUsuarioData toRolUsuarioData(RolUsuario rolUsuario);
}
