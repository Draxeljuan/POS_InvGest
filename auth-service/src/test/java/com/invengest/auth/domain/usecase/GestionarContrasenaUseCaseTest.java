package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.CredencialesInvalidasException;
import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.exception.ReglaNegocioException;
import com.invengest.auth.domain.model.EventoSeguridadCuenta;
import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.NotificacionGateway;
import com.invengest.auth.domain.model.gateway.PasswordEncoderGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionarContrasenaUseCaseTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private PasswordEncoderGateway passwordEncoder;

    @Mock
    private NotificacionGateway notificacionGateway;

    @InjectMocks
    private GestionarContrasenaUseCase gestionarContrasenaUseCase;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(3, "Pedro", "Rojas", "pedro@invengest.local", "300", null,
                new RolUsuario(2, RolUsuario.ROL_VENDEDOR), "pedror", "old_hash", true);
    }

    @Test
    void cambiarContrasenaPropia_ShouldUpdateHashAndNotify_WhenValid() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("pedror")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("ClaveActual123", "old_hash")).thenReturn(true);
        when(passwordEncoder.encode("NuevaClaveSegura456")).thenReturn("new_hash");

        // Act
        gestionarContrasenaUseCase.cambiarContrasenaPropia("pedror", "ClaveActual123", "NuevaClaveSegura456");

        // Assert
        assertEquals("new_hash", usuario.getContrasena());
        verify(usuarioGateway).save(usuario);
        verify(notificacionGateway).enviarCorreoSeguridadCuenta(
                "pedro@invengest.local", "Pedro Rojas", EventoSeguridadCuenta.CAMBIO_CONTRASENA, null);
    }

    @Test
    void cambiarContrasenaPropia_ShouldThrowException_WhenCurrentPasswordInvalid() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("pedror")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("WrongPass", "old_hash")).thenReturn(false);

        // Act & Assert
        assertThrows(CredencialesInvalidasException.class,
                () -> gestionarContrasenaUseCase.cambiarContrasenaPropia("pedror", "WrongPass", "NuevaClaveSegura456"));
    }

    @Test
    void cambiarContrasenaPropia_ShouldThrowException_WhenNewPasswordTooShortOrSame() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("pedror")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("ClaveActual123", "old_hash")).thenReturn(true);

        // Act & Assert
        assertThrows(ReglaNegocioException.class,
                () -> gestionarContrasenaUseCase.cambiarContrasenaPropia("pedror", "ClaveActual123", "corta"));
        assertThrows(ReglaNegocioException.class,
                () -> gestionarContrasenaUseCase.cambiarContrasenaPropia("pedror", "ClaveActual123", "ClaveActual123"));
    }

    @Test
    void restablecerContrasenaPorAdmin_ShouldGenerateTemporaryPasswordAndNotify() {
        // Arrange
        when(usuarioGateway.findById(3)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(any())).thenReturn("temp_hash");

        // Act
        String temporal = gestionarContrasenaUseCase.restablecerContrasenaPorAdmin(3);

        // Assert
        assertNotNull(temporal);
        assertTrue(temporal.startsWith("Tmp#"));
        assertEquals("temp_hash", usuario.getContrasena());
        verify(usuarioGateway).save(usuario);
        verify(notificacionGateway).enviarCorreoSeguridadCuenta(
                eq("pedro@invengest.local"),
                eq("Pedro Rojas"),
                eq(EventoSeguridadCuenta.RESTABLECIMIENTO),
                eq(temporal)
        );
    }

    @Test
    void restablecerContrasenaPorAdmin_ShouldThrowNotFound_WhenUserMissing() {
        // Arrange
        when(usuarioGateway.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class,
                () -> gestionarContrasenaUseCase.restablecerContrasenaPorAdmin(99));
    }
}
