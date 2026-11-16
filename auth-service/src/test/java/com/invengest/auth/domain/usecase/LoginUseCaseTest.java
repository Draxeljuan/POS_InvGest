package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.CredencialesInvalidasException;
import com.invengest.auth.domain.exception.ReglaNegocioException;
import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.PasswordEncoderGateway;
import com.invengest.auth.domain.model.gateway.TokenProviderGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private PasswordEncoderGateway passwordEncoder;

    @Mock
    private TokenProviderGateway tokenProvider;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private Usuario testUsuario;

    @BeforeEach
    void setUp() {
        testUsuario = new Usuario();
        testUsuario.setIdUsuario(1);
        testUsuario.setNombre("Juan");
        testUsuario.setApellido("Draxel");
        testUsuario.setNombreUsuario("JuanD");
        testUsuario.setContrasena("hashed_password");
        testUsuario.setRol(new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR));
        testUsuario.setActivo(true);
    }

    @Test
    void execute_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("JuanD")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CredencialesInvalidasException.class,
                () -> loginUseCase.execute("JuanD", "password123"));
        verify(usuarioGateway, never()).save(any());
    }

    @Test
    void execute_ShouldThrowException_WhenUserIsInactive() {
        // Arrange
        testUsuario.setActivo(false);
        when(usuarioGateway.findByNombreUsuario("JuanD")).thenReturn(Optional.of(testUsuario));

        // Act & Assert
        assertThrows(CredencialesInvalidasException.class,
                () -> loginUseCase.execute("JuanD", "password123"));
        verify(passwordEncoder, never()).matches(any(), any());
        verify(usuarioGateway, never()).save(any());
    }

    @Test
    void execute_ShouldThrowException_WhenPasswordDoesNotMatch() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("JuanD")).thenReturn(Optional.of(testUsuario));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        // Act & Assert
        assertThrows(CredencialesInvalidasException.class,
                () -> loginUseCase.execute("JuanD", "wrong_password"));
        verify(usuarioGateway, never()).save(any());
    }

    @Test
    void execute_ShouldReturnTokenAndSaveAccess_WhenCredentialsAreValid() {
        // Arrange
        when(usuarioGateway.findByNombreUsuario("JuanD")).thenReturn(Optional.of(testUsuario));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(tokenProvider.generateToken(testUsuario)).thenReturn("jwt.token.here");
        when(usuarioGateway.save(any(Usuario.class))).thenReturn(testUsuario);

        // Act
        String token = loginUseCase.execute("JuanD", "password123");

        // Assert
        assertEquals("jwt.token.here", token);
        assertNotNull(testUsuario.getUltimoAcceso());
        verify(usuarioGateway, times(1)).save(testUsuario);
    }

    @Test
    void domainModel_ShouldEnforceBusinessInvariants() {
        // Arrange
        Usuario vendedor = new Usuario(2, "Ana", "Gomez", "ana@mail.com", "300", null,
                new RolUsuario(2, RolUsuario.ROL_VENDEDOR), "anag", "hash", true);

        // Act
        vendedor.desactivar();
        String nombreCompleto = vendedor.obtenerNombreCompleto();

        // Assert
        assertFalse(vendedor.estaActivo());
        assertEquals("Ana Gomez", nombreCompleto);
        assertThrows(ReglaNegocioException.class, () -> testUsuario.desactivar());
        vendedor.activar();
        assertTrue(vendedor.estaActivo());
    }
}
