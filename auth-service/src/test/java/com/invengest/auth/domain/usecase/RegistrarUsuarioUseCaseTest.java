package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.exception.ReglaNegocioException;
import com.invengest.auth.domain.exception.UsuarioYaExisteException;
import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.NotificacionGateway;
import com.invengest.auth.domain.model.gateway.PasswordEncoderGateway;
import com.invengest.auth.domain.model.gateway.RolUsuarioGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioUseCaseTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private RolUsuarioGateway rolUsuarioGateway;

    @Mock
    private PasswordEncoderGateway passwordEncoder;

    @Mock
    private NotificacionGateway notificacionGateway;

    @InjectMocks
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    private Usuario nuevoUsuario;
    private RolUsuario rolVendedor;

    @BeforeEach
    void setUp() {
        rolVendedor = new RolUsuario(2, RolUsuario.ROL_VENDEDOR);
        nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre("Carlos");
        nuevoUsuario.setApellido("Perez");
        nuevoUsuario.setEmail("carlos@invengest.local");
        nuevoUsuario.setNombreUsuario("carlosp");
        nuevoUsuario.setRol(new RolUsuario(2, null));
    }

    @Test
    void execute_ShouldEncodePasswordSaveAndSendWelcomeEmail_WhenValid() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("carlosp")).thenReturn(Optional.empty());
        when(usuarioGateway.findByEmail("carlos@invengest.local")).thenReturn(Optional.empty());
        when(rolUsuarioGateway.findById(2)).thenReturn(Optional.of(rolVendedor));
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_hash");
        when(usuarioGateway.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(10);
            return u;
        });

        // Act
        Usuario resultado = registrarUsuarioUseCase.execute(nuevoUsuario, "Password123!");

        // Assert
        assertEquals(10, resultado.getIdUsuario());
        assertEquals("encoded_hash", resultado.getContrasena());
        assertTrue(resultado.estaActivo());
        assertEquals("Vendedor", resultado.getRol().getNombre());
        verify(notificacionGateway, times(1))
                .enviarCorreoBienvenida("carlos@invengest.local", "Carlos Perez", "carlosp", "Vendedor");
    }

    @Test
    void execute_ShouldThrowException_WhenPasswordTooShort() {
        // Arrange
        String shortPass = "12345";

        // Act & Assert
        assertThrows(ReglaNegocioException.class,
                () -> registrarUsuarioUseCase.execute(nuevoUsuario, shortPass));
        verify(usuarioGateway, never()).save(any());
    }

    @Test
    void execute_ShouldThrowException_WhenUsernameExists() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("carlosp")).thenReturn(Optional.of(nuevoUsuario));

        // Act & Assert
        assertThrows(UsuarioYaExisteException.class,
                () -> registrarUsuarioUseCase.execute(nuevoUsuario, "Password123!"));
        verify(usuarioGateway, never()).save(any());
    }

    @Test
    void execute_ShouldThrowException_WhenEmailExists() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("carlosp")).thenReturn(Optional.empty());
        when(usuarioGateway.findByEmail("carlos@invengest.local")).thenReturn(Optional.of(nuevoUsuario));

        // Act & Assert
        assertThrows(UsuarioYaExisteException.class,
                () -> registrarUsuarioUseCase.execute(nuevoUsuario, "Password123!"));
        verify(usuarioGateway, never()).save(any());
    }

    @Test
    void execute_ShouldThrowException_WhenRoleIsNull() {
        // Arrange
        nuevoUsuario.setRol(null);
        when(usuarioGateway.findByNombreUsuario("carlosp")).thenReturn(Optional.empty());
        when(usuarioGateway.findByEmail("carlos@invengest.local")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ReglaNegocioException.class,
                () -> registrarUsuarioUseCase.execute(nuevoUsuario, "Password123!"));
    }

    @Test
    void execute_ShouldThrowNotFound_WhenRoleDoesNotExist() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("carlosp")).thenReturn(Optional.empty());
        when(usuarioGateway.findByEmail("carlos@invengest.local")).thenReturn(Optional.empty());
        when(rolUsuarioGateway.findById(2)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class,
                () -> registrarUsuarioUseCase.execute(nuevoUsuario, "Password123!"));
    }
}
