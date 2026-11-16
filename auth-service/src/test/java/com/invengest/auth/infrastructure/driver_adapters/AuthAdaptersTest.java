package com.invengest.auth.infrastructure.driver_adapters;

import com.invengest.auth.domain.model.EventoSeguridadCuenta;
import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.PasswordEncoderGateway;
import com.invengest.auth.domain.model.gateway.RolUsuarioGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import com.invengest.auth.infrastructure.driver_adapters.feign.InternalFeignRequestInterceptor;
import com.invengest.auth.infrastructure.driver_adapters.feign.NotificacionFeignAdapter;
import com.invengest.auth.infrastructure.driver_adapters.feign.NotificationFeignClient;
import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.RolUsuarioDataGatewayImpl;
import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.RolUsuarioDataJpaRepository;
import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.UsuarioDataGatewayImpl;
import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.UsuarioDataJpaRepository;
import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.entity.RolUsuarioData;
import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.entity.UsuarioData;
import com.invengest.auth.infrastructure.driver_adapters.security.AdminSeeder;
import com.invengest.auth.infrastructure.driver_adapters.security.PasswordEncoderAdapter;
import com.invengest.auth.infrastructure.mapper.UsuarioEntityMapper;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthAdaptersTest {

    @Mock
    private UsuarioDataJpaRepository usuarioJpaRepository;

    @Mock
    private RolUsuarioDataJpaRepository rolJpaRepository;

    @Mock
    private UsuarioEntityMapper entityMapper;

    @Mock
    private NotificationFeignClient notificationFeignClient;

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private RolUsuarioGateway rolUsuarioGateway;

    @Mock
    private PasswordEncoderGateway passwordEncoderGateway;

    @Test
    void usuarioAndRolGatewayImpl_ShouldDelegateAndMapCorrectly() {
        // Arrange
        UsuarioDataGatewayImpl usuarioGatewayImpl = new UsuarioDataGatewayImpl(usuarioJpaRepository, entityMapper);
        RolUsuarioDataGatewayImpl rolGatewayImpl = new RolUsuarioDataGatewayImpl(rolJpaRepository, entityMapper);

        UsuarioData data = new UsuarioData();
        data.setIdUsuario(1);
        Usuario domain = new Usuario();
        domain.setIdUsuario(1);

        RolUsuarioData rolData = new RolUsuarioData(1, "Administrador");
        RolUsuario rolDomain = new RolUsuario(1, "Administrador");

        when(usuarioJpaRepository.findById(1)).thenReturn(Optional.of(data));
        when(usuarioJpaRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(data));
        when(usuarioJpaRepository.findByEmailIgnoreCase("a@m.com")).thenReturn(Optional.of(data));
        when(usuarioJpaRepository.findAll()).thenReturn(List.of(data));
        when(usuarioJpaRepository.findByRolNombreIgnoreCaseAndActivoTrue("Administrador")).thenReturn(List.of(data));
        when(entityMapper.toUsuario(data)).thenReturn(domain);
        when(entityMapper.toUsuarioData(domain)).thenReturn(data);
        when(usuarioJpaRepository.save(data)).thenReturn(data);

        when(rolJpaRepository.findById(1)).thenReturn(Optional.of(rolData));
        when(rolJpaRepository.findByNombreIgnoreCase("Administrador")).thenReturn(Optional.of(rolData));
        when(rolJpaRepository.findAll()).thenReturn(List.of(rolData));
        when(entityMapper.toRolUsuario(rolData)).thenReturn(rolDomain);

        // Act & Assert
        assertTrue(usuarioGatewayImpl.findById(1).isPresent());
        assertTrue(usuarioGatewayImpl.findByNombreUsuario("admin").isPresent());
        assertTrue(usuarioGatewayImpl.findByEmail("a@m.com").isPresent());
        assertEquals(1, usuarioGatewayImpl.findAll().size());
        assertEquals(1, usuarioGatewayImpl.findActiveByRolNombre("Administrador").size());
        assertEquals(1, usuarioGatewayImpl.save(domain).getIdUsuario());

        assertTrue(rolGatewayImpl.findById(1).isPresent());
        assertTrue(rolGatewayImpl.findByNombre("Administrador").isPresent());
        assertEquals(1, rolGatewayImpl.findAll().size());
    }

    @Test
    void passwordEncoderAdapter_ShouldHashAndMatch() {
        // Arrange
        PasswordEncoderAdapter encoder = new PasswordEncoderAdapter();

        // Act
        String encoded = encoder.encode("SecretPass123!");

        // Assert
        assertNotEquals("SecretPass123!", encoded);
        assertTrue(encoder.matches("SecretPass123!", encoded));
    }

    @Test
    void notificacionFeignAdapter_ShouldExecuteHappyPathAndSwallowRuntimeExceptionGracefully() {
        // Arrange
        NotificacionFeignAdapter adapter = new NotificacionFeignAdapter(notificationFeignClient);

        // Act & Assert (Happy Path)
        assertDoesNotThrow(() -> adapter.enviarCorreoBienvenida("a@m.com", "Juan", "juand", "Vendedor"));
        assertDoesNotThrow(() -> adapter.enviarCorreoSeguridadCuenta(
                "a@m.com", "Juan", EventoSeguridadCuenta.CAMBIO_CONTRASENA, null));

        // Arrange (Failure Path)
        doThrow(new RuntimeException("Connection refused"))
                .when(notificationFeignClient).enviarBienvenida(any());
        doThrow(new RuntimeException("Timeout"))
                .when(notificationFeignClient).enviarSeguridadCuenta(any());

        // Act & Assert (Resilience)
        assertDoesNotThrow(() -> adapter.enviarCorreoBienvenida("a@m.com", "Juan", "juand", "Vendedor"));
        assertDoesNotThrow(() -> adapter.enviarCorreoSeguridadCuenta(
                "a@m.com", "Juan", null, "Tmp#123"));
    }

    @Test
    void internalFeignRequestInterceptor_ShouldAttachHeaderOnlyWhenNonBlank() {
        // Arrange
        InternalFeignRequestInterceptor interceptor = new InternalFeignRequestInterceptor("secret-api-key");
        InternalFeignRequestInterceptor blankInterceptor = new InternalFeignRequestInterceptor("   ");
        RequestTemplate template = new RequestTemplate();
        RequestTemplate emptyTemplate = new RequestTemplate();

        // Act
        interceptor.apply(template);
        blankInterceptor.apply(emptyTemplate);

        // Assert
        assertTrue(template.headers().containsKey(InternalFeignRequestInterceptor.HEADER_INTERNAL_API_KEY));
        assertTrue(emptyTemplate.headers().isEmpty());
    }

    @Test
    void adminSeeder_ShouldCreateAdminOnlyWhenMissingAndConfigured() {
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
    void adminSeeder_ShouldSkipWhenPasswordBlankOrUserExistsOrRoleMissing() {
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
