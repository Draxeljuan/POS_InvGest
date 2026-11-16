package com.invengest.auth.application;

import com.invengest.auth.domain.model.gateway.NotificacionGateway;
import com.invengest.auth.domain.model.gateway.PasswordEncoderGateway;
import com.invengest.auth.domain.model.gateway.RolUsuarioGateway;
import com.invengest.auth.domain.model.gateway.TokenProviderGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import com.invengest.auth.domain.usecase.ActualizarUsuarioUseCase;
import com.invengest.auth.domain.usecase.CambiarEstadoUsuarioUseCase;
import com.invengest.auth.domain.usecase.ConsultarUsuarioUseCase;
import com.invengest.auth.domain.usecase.GestionarContrasenaUseCase;
import com.invengest.auth.domain.usecase.LoginUseCase;
import com.invengest.auth.domain.usecase.RegistrarUsuarioUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public LoginUseCase loginUseCase(UsuarioGateway usuarioGateway,
                                     PasswordEncoderGateway passwordEncoder,
                                     TokenProviderGateway tokenProvider) {
        return new LoginUseCase(usuarioGateway, passwordEncoder, tokenProvider);
    }

    @Bean
    public RegistrarUsuarioUseCase registrarUsuarioUseCase(UsuarioGateway usuarioGateway,
                                                           RolUsuarioGateway rolUsuarioGateway,
                                                           PasswordEncoderGateway passwordEncoder,
                                                           NotificacionGateway notificacionGateway) {
        return new RegistrarUsuarioUseCase(usuarioGateway, rolUsuarioGateway, passwordEncoder, notificacionGateway);
    }

    @Bean
    public ActualizarUsuarioUseCase actualizarUsuarioUseCase(UsuarioGateway usuarioGateway,
                                                             RolUsuarioGateway rolUsuarioGateway) {
        return new ActualizarUsuarioUseCase(usuarioGateway, rolUsuarioGateway);
    }

    @Bean
    public CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase(UsuarioGateway usuarioGateway,
                                                                   NotificacionGateway notificacionGateway) {
        return new CambiarEstadoUsuarioUseCase(usuarioGateway, notificacionGateway);
    }

    @Bean
    public GestionarContrasenaUseCase gestionarContrasenaUseCase(UsuarioGateway usuarioGateway,
                                                                 PasswordEncoderGateway passwordEncoder,
                                                                 NotificacionGateway notificacionGateway) {
        return new GestionarContrasenaUseCase(usuarioGateway, passwordEncoder, notificacionGateway);
    }

    @Bean
    public ConsultarUsuarioUseCase consultarUsuarioUseCase(UsuarioGateway usuarioGateway,
                                                           RolUsuarioGateway rolUsuarioGateway) {
        return new ConsultarUsuarioUseCase(usuarioGateway, rolUsuarioGateway);
    }
}
