package com.invengest.auth.infrastructure.driver_adapters.jpa_repository;

import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.entity.UsuarioData;
import com.invengest.auth.infrastructure.mapper.UsuarioEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDataGatewayImplTest {

    @Mock
    private UsuarioDataJpaRepository usuarioJpaRepository;

    @Mock
    private UsuarioEntityMapper entityMapper;

    @InjectMocks
    private UsuarioDataGatewayImpl usuarioGatewayImpl;

    @Test
    @DisplayName("Debe delegar consultas y guardado al repositorio JPA y mapear entidades")
    void operacionesCrud_DebenDelegarYMapearCorrectamente() {
        // Arrange
        UsuarioData data = new UsuarioData();
        data.setIdUsuario(1);
        Usuario domain = new Usuario();
        domain.setIdUsuario(1);

        when(usuarioJpaRepository.findById(1)).thenReturn(Optional.of(data));
        when(usuarioJpaRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(data));
        when(usuarioJpaRepository.findByEmailIgnoreCase("a@m.com")).thenReturn(Optional.of(data));
        when(usuarioJpaRepository.findAll()).thenReturn(List.of(data));
        when(usuarioJpaRepository.findByRolNombreIgnoreCaseAndActivoTrue("Administrador")).thenReturn(List.of(data));
        when(entityMapper.toUsuario(data)).thenReturn(domain);
        when(entityMapper.toUsuarioData(domain)).thenReturn(data);
        when(usuarioJpaRepository.save(data)).thenReturn(data);

        // Act
        Optional<Usuario> porId = usuarioGatewayImpl.findById(1);
        Optional<Usuario> porUsername = usuarioGatewayImpl.findByNombreUsuario("admin");
        Optional<Usuario> porEmail = usuarioGatewayImpl.findByEmail("a@m.com");
        List<Usuario> todos = usuarioGatewayImpl.findAll();
        List<Usuario> adminsActivos = usuarioGatewayImpl.findActiveByRolNombre("Administrador");
        Usuario guardado = usuarioGatewayImpl.save(domain);

        // Assert
        assertTrue(porId.isPresent());
        assertTrue(porUsername.isPresent());
        assertTrue(porEmail.isPresent());
        assertEquals(1, todos.size());
        assertEquals(1, adminsActivos.size());
        assertEquals(1, guardado.getIdUsuario());
    }
}
