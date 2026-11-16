package com.invengest.auth.infrastructure.driver_adapters.security;

import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.model.gateway.PasswordEncoderGateway;
import com.invengest.auth.domain.model.gateway.RolUsuarioGateway;
import com.invengest.auth.domain.model.gateway.UsuarioGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UsuarioGateway usuarioGateway;
    private final RolUsuarioGateway rolUsuarioGateway;
    private final PasswordEncoderGateway passwordEncoderGateway;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminEmail;
    private final String adminNombre;
    private final String adminApellido;
    private final String adminTelefono;

    public AdminSeeder(
            UsuarioGateway usuarioGateway,
            RolUsuarioGateway rolUsuarioGateway,
            PasswordEncoderGateway passwordEncoderGateway,
            @Value("${admin.username:admin}") String adminUsername,
            @Value("${admin.password:}") String adminPassword,
            @Value("${admin.email:admin@invengest.local}") String adminEmail,
            @Value("${admin.nombre:Administrador}") String adminNombre,
            @Value("${admin.apellido:Principal}") String adminApellido,
            @Value("${admin.telefono:3000000000}") String adminTelefono) {
        this.usuarioGateway = usuarioGateway;
        this.rolUsuarioGateway = rolUsuarioGateway;
        this.passwordEncoderGateway = passwordEncoderGateway;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminEmail = adminEmail;
        this.adminNombre = adminNombre;
        this.adminApellido = adminApellido;
        this.adminTelefono = adminTelefono;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.info("admin_seeder_omitido motivo=ADMIN_PASSWORD_no_definido");
            return;
        }

        if (usuarioGateway.findByNombreUsuario(adminUsername).isPresent()) {
            log.info("admin_seeder_omitido motivo=usuario_ya_existe username={}", adminUsername);
            return;
        }

        Optional<RolUsuario> rolAdminOpt = rolUsuarioGateway.findByNombre(RolUsuario.ROL_ADMINISTRADOR);
        if (rolAdminOpt.isEmpty()) {
            log.warn("admin_seeder_omitido motivo=rol_administrador_no_encontrado");
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre(adminNombre);
        admin.setApellido(adminApellido);
        admin.setEmail(adminEmail);
        admin.setTelefono(adminTelefono);
        admin.setNombreUsuario(adminUsername);
        admin.setContrasena(passwordEncoderGateway.encode(adminPassword));
        admin.setRol(rolAdminOpt.get());
        admin.activar();

        usuarioGateway.save(admin);
        log.info("admin_seeder_creado username={} email={}", adminUsername, adminEmail);
    }
}
