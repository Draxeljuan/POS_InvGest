package com.invengest.auth.infrastructure.entry_points;

import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.usecase.ConsultarUsuarioUseCase;
import com.invengest.auth.infrastructure.entry_points.dto.ContactoUsuarioDTO;
import com.invengest.auth.infrastructure.mapper.UsuarioRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalUsuarioControllerTest {

    @Mock
    private ConsultarUsuarioUseCase consultarUsuarioUseCase;

    @Mock
    private UsuarioRestMapper usuarioRestMapper;

    @InjectMocks
    private InternalUsuarioController internalUsuarioController;

    @Test
    @DisplayName("Debe retornar datos de contacto de un usuario y lista de administradores activos")
    void endpointsInternos_DebenRetornarContactosMapeados() {
        // Arrange
        Usuario usuarioDominio = new Usuario(1, "Juan", "Draxel", "juan@invengest.local", "300", null,
                new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR), "juand", "hash", true);
        ContactoUsuarioDTO contactoDTO = new ContactoUsuarioDTO(1, "Juan Draxel", "juan@invengest.local");

        when(consultarUsuarioUseCase.obtenerPorId(1)).thenReturn(usuarioDominio);
        when(consultarUsuarioUseCase.listarAdministradoresActivos()).thenReturn(List.of(usuarioDominio));
        when(usuarioRestMapper.toContactoResponse(usuarioDominio)).thenReturn(contactoDTO);

        // Act
        ResponseEntity<ContactoUsuarioDTO> contactoRes = internalUsuarioController.obtenerContactoUsuario(1);
        ResponseEntity<List<ContactoUsuarioDTO>> adminsRes = internalUsuarioController.obtenerContactosAdministradores();

        // Assert
        assertEquals(HttpStatus.OK, contactoRes.getStatusCode());
        assertNotNull(contactoRes.getBody());
        assertEquals("Juan Draxel", contactoRes.getBody().nombreCompleto());
        assertEquals(HttpStatus.OK, adminsRes.getStatusCode());
        assertNotNull(adminsRes.getBody());
        assertEquals(1, adminsRes.getBody().size());
        verify(consultarUsuarioUseCase).listarAdministradoresActivos();
    }
}
