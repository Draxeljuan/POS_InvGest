package com.invengest.auth.infrastructure.mapper;

import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.infrastructure.entry_points.dto.ActualizarUsuarioRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.ContactoUsuarioDTO;
import com.invengest.auth.infrastructure.entry_points.dto.RegistrarUsuarioRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.RolResponseDTO;
import com.invengest.auth.infrastructure.entry_points.dto.UsuarioResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioRestMapper {

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "ultimoAcceso", ignore = true)
    @Mapping(target = "contrasena", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "nombreUsuario", source = "username")
    @Mapping(target = "rol.idRol", source = "idRol")
    Usuario toDomain(RegistrarUsuarioRequestDTO request);

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "ultimoAcceso", ignore = true)
    @Mapping(target = "nombreUsuario", ignore = true)
    @Mapping(target = "contrasena", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "rol.idRol", source = "idRol")
    Usuario toDomain(ActualizarUsuarioRequestDTO request);

    @Mapping(target = "username", source = "nombreUsuario")
    @Mapping(target = "rol", source = "rol.nombre")
    UsuarioResponseDTO toResponse(Usuario usuario);

    @Mapping(target = "nombreCompleto", expression = "java(usuario.obtenerNombreCompleto())")
    ContactoUsuarioDTO toContactoResponse(Usuario usuario);

    RolResponseDTO toRolResponse(RolUsuario rolUsuario);
}
