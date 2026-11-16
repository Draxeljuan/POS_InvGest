package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.RolUsuarioGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarUsuarioUseCaseTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private RolUsuarioGateway rolUsuarioGateway;

    @InjectMocks
    private ConsultarUsuarioUseCase consultarUsuarioUseCase;

    private Usuario admin;

    @BeforeEach
    void setUp() {
        admin = new Usuario(1, "Admin", "P", "admin@invengest.local", "300", null,
                new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR), "admin", "hash", true);
    }

    @Test
    void obtenerPorId_ShouldReturnUser_WhenExists() {
        // Arrange
        when(usuarioGateway.findById(1)).thenReturn(Optional.of(admin));

        // Act
        Usuario result = consultarUsuarioUseCase.obtenerPorId(1);

        // Assert
        assertEquals("admin", result.getNombreUsuario());
    }

    @Test
    void obtenerPorId_ShouldThrowNotFound_WhenMissing() {
        // Arrange
        when(usuarioGateway.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> consultarUsuarioUseCase.obtenerPorId(99));
    }

    @Test
    void obtenerPorUsername_ShouldReturnUser_WhenExists() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("admin")).thenReturn(Optional.of(admin));

        // Act
        Usuario result = consultarUsuarioUseCase.obtenerPorUsername("admin");

        // Assert
        assertEquals(1, result.getIdUsuario());
    }

    @Test
    void obtenerPorUsername_ShouldThrowNotFound_WhenMissing() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("missing")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> consultarUsuarioUseCase.obtenerPorUsername("missing"));
    }

    @Test
    void listarUsuariosYRolesYAdmins_ShouldReturnLists() {
        // Arrange
        when(usuarioGateway.findAll()).thenReturn(List.of(admin));
        when(usuarioGateway.findActiveByRolNombre(RolUsuario.ROL_ADMINISTRADOR)).thenReturn(List.of(admin));
        when(rolUsuarioGateway.findAll()).thenReturn(List.of(new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR)));

        // Act
        List<Usuario> usuarios = consultarUsuarioUseCase.listarUsuarios();
        List<Usuario> admins = consultarUsuarioUseCase.listarAdministradoresActivos();
        List<RolUsuario> roles = consultarUsuarioUseCase.listarRoles();

        // Assert
        assertEquals(1, usuarios.size());
        assertEquals(1, admins.size());
        assertEquals(1, roles.size());
    }
}
