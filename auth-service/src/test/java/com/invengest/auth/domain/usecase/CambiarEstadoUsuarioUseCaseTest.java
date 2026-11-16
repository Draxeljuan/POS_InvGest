package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.exception.ReglaNegocioException;
import com.invengest.auth.domain.model.EventoSeguridadCuenta;
import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.NotificacionGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CambiarEstadoUsuarioUseCaseTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private NotificacionGateway notificacionGateway;

    @InjectMocks
    private CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;

    private Usuario vendedor;
    private Usuario administrador;

    @BeforeEach
    void setUp() {
        vendedor = new Usuario(2, "Laura", "Vega", "laura@invengest.local", "300", null,
                new RolUsuario(2, RolUsuario.ROL_VENDEDOR), "laurav", "hash", true);

        administrador = new Usuario(1, "Admin", "Root", "admin@invengest.local", "300", null,
                new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR), "admin", "hash", true);
    }

    @Test
    void execute_ShouldDeactivateVendorAndNotify_WhenAdminRequests() {
        // Arrange
        when(usuarioGateway.findById(2)).thenReturn(Optional.of(vendedor));
        when(usuarioGateway.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Usuario resultado = cambiarEstadoUsuarioUseCase.execute(2, false, "admin");

        // Assert
        assertFalse(resultado.estaActivo());
        verify(notificacionGateway).enviarCorreoSeguridadCuenta(
                "laura@invengest.local", "Laura Vega", EventoSeguridadCuenta.DESACTIVACION, null);
    }

    @Test
    void execute_ShouldActivateVendor_WhenRequested() {
        // Arrange
        vendedor.setActivo(false);
        when(usuarioGateway.findById(2)).thenReturn(Optional.of(vendedor));
        when(usuarioGateway.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Usuario resultado = cambiarEstadoUsuarioUseCase.execute(2, true, "admin");

        // Assert
        assertTrue(resultado.estaActivo());
        verify(notificacionGateway, never()).enviarCorreoSeguridadCuenta(any(), any(), any(), any());
    }

    @Test
    void execute_ShouldThrowException_WhenUserTriesToDeactivateSelf() {
        // Arrange
        when(usuarioGateway.findById(2)).thenReturn(Optional.of(vendedor));

        // Act & Assert
        assertThrows(ReglaNegocioException.class,
                () -> cambiarEstadoUsuarioUseCase.execute(2, false, "laurav"));
        verify(usuarioGateway, never()).save(any());
    }

    @Test
    void execute_ShouldThrowException_WhenTryingToDeactivateAdministrator() {
        // Arrange
        when(usuarioGateway.findById(1)).thenReturn(Optional.of(administrador));

        // Act & Assert
        assertThrows(ReglaNegocioException.class,
                () -> cambiarEstadoUsuarioUseCase.execute(1, false, "otro_admin"));
        verify(usuarioGateway, never()).save(any());
    }

    @Test
    void execute_ShouldThrowNotFound_WhenUserDoesNotExist() {
        // Arrange
        when(usuarioGateway.findById(404)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class,
                () -> cambiarEstadoUsuarioUseCase.execute(404, false, "admin"));
    }
}
