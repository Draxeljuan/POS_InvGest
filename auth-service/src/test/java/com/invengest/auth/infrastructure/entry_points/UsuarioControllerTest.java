package com.invengest.auth.infrastructure.entry_points;

import com.invengest.auth.domain.exception.CredencialesInvalidasException;
import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.exception.ReglaNegocioException;
import com.invengest.auth.domain.exception.UsuarioYaExisteException;
import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.usecase.ActualizarUsuarioUseCase;
import com.invengest.auth.domain.usecase.CambiarEstadoUsuarioUseCase;
import com.invengest.auth.domain.usecase.ConsultarUsuarioUseCase;
import com.invengest.auth.domain.usecase.GestionarContrasenaUseCase;
import com.invengest.auth.domain.usecase.LoginUseCase;
import com.invengest.auth.domain.usecase.RegistrarUsuarioUseCase;
import com.invengest.auth.infrastructure.entry_points.advice.ErrorResponseDTO;
import com.invengest.auth.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.auth.infrastructure.entry_points.dto.ActualizarUsuarioRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.CambiarContrasenaRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.CambiarEstadoUsuarioRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.ContactoUsuarioDTO;
import com.invengest.auth.infrastructure.entry_points.dto.LoginRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.MensajeResponseDTO;
import com.invengest.auth.infrastructure.entry_points.dto.RegistrarUsuarioRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.RestablecerContrasenaResponseDTO;
import com.invengest.auth.infrastructure.entry_points.dto.RolResponseDTO;
import com.invengest.auth.infrastructure.entry_points.dto.TokenResponseDTO;
import com.invengest.auth.infrastructure.entry_points.dto.UsuarioResponseDTO;
import com.invengest.auth.infrastructure.mapper.UsuarioRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @Mock
    private ActualizarUsuarioUseCase actualizarUsuarioUseCase;

    @Mock
    private CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;

    @Mock
    private GestionarContrasenaUseCase gestionarContrasenaUseCase;

    @Mock
    private ConsultarUsuarioUseCase consultarUsuarioUseCase;

    @Mock
    private UsuarioRestMapper usuarioRestMapper;

    @InjectMocks
    private UsuarioController usuarioController;

    @InjectMocks
    private InternalUsuarioController internalUsuarioController;

    private Usuario usuarioDominio;
    private UsuarioResponseDTO usuarioResponseDTO;

    @BeforeEach
    void setUp() {
        usuarioDominio = new Usuario(1, "Juan", "Draxel", "juan@invengest.local", "300", null,
                new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR), "juand", "hash", true);
        usuarioResponseDTO = new UsuarioResponseDTO(1, "Juan", "Draxel", "juan@invengest.local",
                "300", "juand", "Administrador", true, null);
    }

    @Test
    void login_ShouldReturn200WithToken() {
        // Arrange
        LoginRequestDTO req = new LoginRequestDTO("juand", "Password123");
        when(loginUseCase.execute("juand", "Password123")).thenReturn("jwt-123");

        // Act
        ResponseEntity<TokenResponseDTO> res = usuarioController.login(req);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("jwt-123", res.getBody().token());
    }

    @Test
    void register_ShouldReturn201Created() {
        // Arrange
        RegistrarUsuarioRequestDTO req = new RegistrarUsuarioRequestDTO(
                "Juan", "Draxel", "juan@invengest.local", "300", "juand", "Password123", 1);
        when(usuarioRestMapper.toDomain(req)).thenReturn(usuarioDominio);
        when(registrarUsuarioUseCase.execute(usuarioDominio, "Password123")).thenReturn(usuarioDominio);
        when(usuarioRestMapper.toResponse(usuarioDominio)).thenReturn(usuarioResponseDTO);

        // Act
        ResponseEntity<UsuarioResponseDTO> res = usuarioController.register(req);

        // Assert
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("juand", res.getBody().username());
    }

    @Test
    void meAndContrasenaAndResetAndEstado_ShouldExecuteCorrectly() {
        // Arrange
        Authentication auth = new UsernamePasswordAuthenticationToken("juand", null);
        when(consultarUsuarioUseCase.obtenerPorUsername("juand")).thenReturn(usuarioDominio);
        when(usuarioRestMapper.toResponse(usuarioDominio)).thenReturn(usuarioResponseDTO);
        doNothing().when(gestionarContrasenaUseCase).cambiarContrasenaPropia("juand", "OldPass123", "NewPass456");
        when(gestionarContrasenaUseCase.restablecerContrasenaPorAdmin(2)).thenReturn("Tmp#12345678");
        when(cambiarEstadoUsuarioUseCase.execute(2, false, "juand")).thenReturn(usuarioDominio);

        // Act
        ResponseEntity<UsuarioResponseDTO> resMe = usuarioController.obtenerPerfilActual(auth);
        ResponseEntity<MensajeResponseDTO> resPass = usuarioController.cambiarContrasenaPropia(
                auth, new CambiarContrasenaRequestDTO("OldPass123", "NewPass456"));
        ResponseEntity<RestablecerContrasenaResponseDTO> resReset = usuarioController.restablecerContrasena(2);
        ResponseEntity<UsuarioResponseDTO> resEstado = usuarioController.cambiarEstadoUsuario(
                2, new CambiarEstadoUsuarioRequestDTO(false), auth);

        // Assert
        assertEquals(HttpStatus.OK, resMe.getStatusCode());
        assertEquals(HttpStatus.OK, resPass.getStatusCode());
        assertEquals("Tmp#12345678", resReset.getBody().contrasenaTemporal());
        assertEquals(HttpStatus.OK, resEstado.getStatusCode());
    }

    @Test
    void crudAndInternalEndpoints_ShouldReturnMappedResponses() {
        // Arrange
        ActualizarUsuarioRequestDTO updateReq = new ActualizarUsuarioRequestDTO(
                "Juan", "Draxel", "juan@invengest.local", "300", 1);
        RolUsuario rol = new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR);
        RolResponseDTO rolDTO = new RolResponseDTO(1, "Administrador");
        ContactoUsuarioDTO contactoDTO = new ContactoUsuarioDTO(1, "Juan Draxel", "juan@invengest.local");

        when(consultarUsuarioUseCase.listarUsuarios()).thenReturn(List.of(usuarioDominio));
        when(consultarUsuarioUseCase.obtenerPorId(1)).thenReturn(usuarioDominio);
        when(usuarioRestMapper.toDomain(updateReq)).thenReturn(usuarioDominio);
        when(actualizarUsuarioUseCase.execute(eq(1), any(Usuario.class))).thenReturn(usuarioDominio);
        when(usuarioRestMapper.toResponse(usuarioDominio)).thenReturn(usuarioResponseDTO);
        when(consultarUsuarioUseCase.listarRoles()).thenReturn(List.of(rol));
        when(usuarioRestMapper.toRolResponse(rol)).thenReturn(rolDTO);
        when(usuarioRestMapper.toContactoResponse(usuarioDominio)).thenReturn(contactoDTO);
        when(consultarUsuarioUseCase.listarAdministradoresActivos()).thenReturn(List.of(usuarioDominio));

        // Act
        ResponseEntity<List<UsuarioResponseDTO>> listRes = usuarioController.listarUsuarios();
        ResponseEntity<UsuarioResponseDTO> getRes = usuarioController.obtenerUsuarioPorId(1);
        ResponseEntity<UsuarioResponseDTO> putRes = usuarioController.actualizarUsuario(1, updateReq);
        ResponseEntity<List<RolResponseDTO>> rolesRes = usuarioController.listarRoles();
        ResponseEntity<ContactoUsuarioDTO> contactoRes = internalUsuarioController.obtenerContactoUsuario(1);
        ResponseEntity<List<ContactoUsuarioDTO>> adminsRes = internalUsuarioController.obtenerContactosAdministradores();

        // Assert
        assertEquals(1, listRes.getBody().size());
        assertEquals("juand", getRes.getBody().username());
        assertEquals("juand", putRes.getBody().username());
        assertEquals(1, rolesRes.getBody().size());
        assertEquals("Juan Draxel", contactoRes.getBody().nombreCompleto());
        assertEquals(1, adminsRes.getBody().size());
        verify(consultarUsuarioUseCase).listarAdministradoresActivos();
    }

    @Test
    void globalExceptionHandler_ShouldMapAllDomainAndTechnicalExceptions() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        ResponseEntity<ErrorResponseDTO> r401 = handler.handleInvalidCredentials(new CredencialesInvalidasException("Credenciales"));
        ResponseEntity<ErrorResponseDTO> r404 = handler.handleNotFound(new RecursoNoEncontradoException("No existe"));
        ResponseEntity<ErrorResponseDTO> r409 = handler.handleUserAlreadyExists(new UsuarioYaExisteException("Duplicado"));
        ResponseEntity<ErrorResponseDTO> r400 = handler.handleBusinessRule(new ReglaNegocioException("Regla"));
        ResponseEntity<ErrorResponseDTO> rDb = handler.handleDatabaseConstraints(new DataIntegrityViolationException("FK"));
        ResponseEntity<ErrorResponseDTO> r500 = handler.handleGenericException(new RuntimeException("Fallo"));

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, r401.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, r404.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, r409.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, r400.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rDb.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r500.getStatusCode());
    }
}
