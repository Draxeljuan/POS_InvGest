package com.invengest.auth.infrastructure.driver_adapters.jpa_repository;

import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.entity.UsuarioData;
import com.invengest.auth.infrastructure.mapper.UsuarioEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioDataGatewayImpl implements UsuarioGateway {

    private final UsuarioDataJpaRepository usuarioDataJpaRepository;
    private final UsuarioEntityMapper usuarioEntityMapper;

    @Override
    public Optional<Usuario> findById(Integer idUsuario) {
        return usuarioDataJpaRepository.findById(idUsuario)
                .map(usuarioEntityMapper::toUsuario);
    }

    @Override
    public Optional<Usuario> findByNombreUsuario(String nombreUsuario) {
        return usuarioDataJpaRepository.findByNombreUsuario(nombreUsuario)
                .map(usuarioEntityMapper::toUsuario);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioDataJpaRepository.findByEmailIgnoreCase(email)
                .map(usuarioEntityMapper::toUsuario);
    }

    @Override
    public List<Usuario> findAll() {
        return usuarioDataJpaRepository.findAll()
                .stream()
                .map(usuarioEntityMapper::toUsuario)
                .toList();
    }

    @Override
    public List<Usuario> findActiveByRolNombre(String nombreRol) {
        return usuarioDataJpaRepository.findByRolNombreIgnoreCaseAndActivoTrue(nombreRol)
                .stream()
                .map(usuarioEntityMapper::toUsuario)
                .toList();
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioData entityToSave = usuarioEntityMapper.toUsuarioData(usuario);
        UsuarioData savedEntity = usuarioDataJpaRepository.save(entityToSave);
        return usuarioEntityMapper.toUsuario(savedEntity);
    }
}
