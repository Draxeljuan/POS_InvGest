package com.invengest.auth.infrastructure.driver_adapters.jpa_repository;

import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.gateway.RolUsuarioGateway;
import com.invengest.auth.infrastructure.mapper.UsuarioEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RolUsuarioDataGatewayImpl implements RolUsuarioGateway {

    private final RolUsuarioDataJpaRepository rolUsuarioDataJpaRepository;
    private final UsuarioEntityMapper usuarioEntityMapper;

    @Override
    public Optional<RolUsuario> findById(Integer idRol) {
        return rolUsuarioDataJpaRepository.findById(idRol)
                .map(usuarioEntityMapper::toRolUsuario);
    }

    @Override
    public Optional<RolUsuario> findByNombre(String nombre) {
        return rolUsuarioDataJpaRepository.findByNombreIgnoreCase(nombre)
                .map(usuarioEntityMapper::toRolUsuario);
    }

    @Override
    public List<RolUsuario> findAll() {
        return rolUsuarioDataJpaRepository.findAll()
                .stream()
                .map(usuarioEntityMapper::toRolUsuario)
                .toList();
    }
}
