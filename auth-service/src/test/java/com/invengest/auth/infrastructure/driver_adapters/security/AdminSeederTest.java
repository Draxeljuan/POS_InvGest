package com.invengest.auth.infrastructure.driver_adapters.security;

import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.PasswordEncoderGateway;
import com.invengest.auth.domain.model.gateway.RolUsuarioGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private RolUsuarioGateway rolUsuarioGateway;

    @Mock
    private PasswordEncoderGateway passwordEncoderGateway;

    @Test
    @DisplayName("Debe crear el administrador inicial cuando no existe y la contraseña está configurada")
    void run_CuandoAdminNoExisteYClaveConfigurada_DebeGuardarUsuario() {
        // Arrange
        AdminSeeder seeder = new AdminSeeder(
                usuarioGateway, rolUsuarioGateway, passwordEncoderGateway,
                "admin", "AdminSecret123", "admin@invengest.local", "Admin", "Principal", "300");

        when(usuarioGateway.findByNombreUsuario("admin")).thenReturn(Optional.empty());
        when(rolUsuarioGateway.findByNombre(RolUsuario.ROL_ADMINISTRADOR))
                .thenReturn(Optional.of(new RolUsuario(1, RolUsuario.ROL_ADMINISTRADOR)));
        when(passwordEncoderGateway.encode("AdminSecret123")).thenReturn("hashed");

        // Act
        seeder.run(new DefaultApplicationArguments());

        // Assert
        verify(usuarioGateway, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe omitir la creación cuando la clave está vacía, el usuario ya existe o falta el rol")
    void run_CuandoClaveVaciaOUsuarioExisteORolFalta_DebeOmitir() {
        // Arrange
        AdminSeeder seederSinClave = new AdminSeeder(
                usuarioGateway, rolUsuarioGateway, passwordEncoderGateway,
                "admin", "", "admin@invengest.local", "Admin", "Principal", "300");
        AdminSeeder seederExistente = new AdminSeeder(
                usuarioGateway, rolUsuarioGateway, passwordEncoderGateway,
                "admin", "Pass1234", "admin@invengest.local", "Admin", "Principal", "300");
        AdminSeeder seederSinRol = new AdminSeeder(
                usuarioGateway, rolUsuarioGateway, passwordEncoderGateway,
                "nuevo_admin", "Pass1234", "admin@invengest.local", "Admin", "Principal", "300");

        when(usuarioGateway.findByNombreUsuario("admin")).thenReturn(Optional.of(new Usuario()));
        when(usuarioGateway.findByNombreUsuario("nuevo_admin")).thenReturn(Optional.empty());
        when(rolUsuarioGateway.findByNombre(RolUsuario.ROL_ADMINISTRADOR)).thenReturn(Optional.empty());

        // Act
        seederSinClave.run(new DefaultApplicationArguments());
        seederExistente.run(new DefaultApplicationArguments());
        seederSinRol.run(new DefaultApplicationArguments());

        // Assert
        verify(usuarioGateway, never()).save(any());
    }
}
