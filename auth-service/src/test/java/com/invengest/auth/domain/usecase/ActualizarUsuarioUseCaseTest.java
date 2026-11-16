package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.exception.UsuarioYaExisteException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActualizarUsuarioUseCaseTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private RolUsuarioGateway rolUsuarioGateway;

    @InjectMocks
    private ActualizarUsuarioUseCase actualizarUsuarioUseCase;

    private Usuario existente;

    @BeforeEach
    void setUp() {
        existente = new Usuario();
        existente.setIdUsuario(5);
        existente.setNombre("Mario");
        existente.setApellido("Lopez");
        existente.setEmail("mario@invengest.local");
        existente.setTelefono("3100000000");
        existente.setRol(new RolUsuario(2, RolUsuario.ROL_VENDEDOR));
    }

    @Test
    void execute_ShouldUpdateFieldsAndSave_WhenValid() {
        // Arrange
        Usuario cambios = new Usuario();
        cambios.setNombre("Mario Alberto");
        cambios.setApellido("Lopez Diaz");
        cambios.setEmail("nuevo@invengest.local");
        cambios.setTelefono("3200000000");
        cambios.setRol(new RolUsuario(1, null));

        when(usuarioGateway.findById(5)).thenReturn(Optional.of(existente));
        when(usuarioGateway.findByEmail("nuevo@invengest.local")).thenReturn(Optional.empty());
        when(rolUsuarioGateway.findById(1)).thenReturn(Optional.of(new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR)));
        when(usuarioGateway.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Usuario actualizado = actualizarUsuarioUseCase.execute(5, cambios);

        // Assert
        assertEquals("Mario Alberto", actualizado.getNombre());
        assertEquals("Lopez Diaz", actualizado.getApellido());
        assertEquals("nuevo@invengest.local", actualizado.getEmail());
        assertEquals("Administrador", actualizado.getRol().getNombre());
        verify(usuarioGateway).save(existente);
    }

    @Test
    void execute_ShouldThrowNotFound_WhenUserDoesNotExist() {
        // Arrange
        when(usuarioGateway.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class,
                () -> actualizarUsuarioUseCase.execute(99, new Usuario()));
    }

    @Test
    void execute_ShouldThrowConflict_WhenEmailBelongsToAnotherUser() {
        // Arrange
        Usuario otro = new Usuario();
        otro.setIdUsuario(8);
        otro.setEmail("duplicado@invengest.local");

        Usuario cambios = new Usuario();
        cambios.setEmail("duplicado@invengest.local");

        when(usuarioGateway.findById(5)).thenReturn(Optional.of(existente));
        when(usuarioGateway.findByEmail("duplicado@invengest.local")).thenReturn(Optional.of(otro));

        // Act & Assert
        assertThrows(UsuarioYaExisteException.class,
                () -> actualizarUsuarioUseCase.execute(5, cambios));
    }

    @Test
    void execute_ShouldThrowNotFound_WhenNewRoleDoesNotExist() {
        // Arrange
        Usuario cambios = new Usuario();
        cambios.setRol(new RolUsuario(99, null));

        when(usuarioGateway.findById(5)).thenReturn(Optional.of(existente));
        when(rolUsuarioGateway.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class,
                () -> actualizarUsuarioUseCase.execute(5, cambios));
    }

    @Test
    void execute_ShouldKeepExistingFields_WhenPartialOrSameEmailUpdate() {
        // Arrange
        Usuario cambiosParciales = new Usuario();
        cambiosParciales.setEmail("mario@invengest.local");
        when(usuarioGateway.findById(5)).thenReturn(Optional.of(existente));
        when(usuarioGateway.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Usuario resultado = actualizarUsuarioUseCase.execute(5, cambiosParciales);

        // Assert
        assertEquals("Mario", resultado.getNombre());
        assertEquals("Lopez", resultado.getApellido());
        assertEquals("mario@invengest.local", resultado.getEmail());
    }
}
