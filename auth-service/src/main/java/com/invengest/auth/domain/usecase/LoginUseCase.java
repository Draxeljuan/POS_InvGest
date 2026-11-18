package com.invengest.auth.domain.usecase;

import com.invengest.auth.domain.exception.CredencialesInvalidasException;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.PasswordEncoderGateway;
import com.invengest.auth.domain.model.gateway.TokenProviderGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RequiredArgsConstructor
public class LoginUseCase {

    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos";

    private final UsuarioGateway usuarioGateway;
    private final PasswordEncoderGateway passwordEncoder;
    private final TokenProviderGateway tokenProvider;

    public String execute(String username, String rawPassword) {
        Usuario usuario = usuarioGateway.findByNombreUsuario(username)
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS));

        if (!usuario.estaActivo()) {
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
        }

        if (!passwordEncoder.matches(rawPassword, usuario.getContrasena())) {
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
        }

        usuario.registrarUltimoAcceso(LocalDateTime.now(ZoneId.of("America/Bogota")));
        usuarioGateway.save(usuario);

        return tokenProvider.generateToken(usuario);
    }
}
