package com.invengest.auth.infrastructure.entry_points;

import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.usecase.ActualizarUsuarioUseCase;
import com.invengest.auth.domain.usecase.CambiarEstadoUsuarioUseCase;
import com.invengest.auth.domain.usecase.ConsultarUsuarioUseCase;
import com.invengest.auth.domain.usecase.GestionarContrasenaUseCase;
import com.invengest.auth.domain.usecase.LoginUseCase;
import com.invengest.auth.domain.usecase.RegistrarUsuarioUseCase;
import com.invengest.auth.infrastructure.entry_points.dto.ActualizarUsuarioRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.CambiarContrasenaRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.CambiarEstadoUsuarioRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.LoginRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.MensajeResponseDTO;
import com.invengest.auth.infrastructure.entry_points.dto.RegistrarUsuarioRequestDTO;
import com.invengest.auth.infrastructure.entry_points.dto.RestablecerContrasenaResponseDTO;
import com.invengest.auth.infrastructure.entry_points.dto.RolResponseDTO;
import com.invengest.auth.infrastructure.entry_points.dto.TokenResponseDTO;
import com.invengest.auth.infrastructure.entry_points.dto.UsuarioResponseDTO;
import com.invengest.auth.infrastructure.mapper.UsuarioRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UsuarioController {

    private final LoginUseCase loginUseCase;
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final ActualizarUsuarioUseCase actualizarUsuarioUseCase;
    private final CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;
    private final GestionarContrasenaUseCase gestionarContrasenaUseCase;
    private final ConsultarUsuarioUseCase consultarUsuarioUseCase;
    private final UsuarioRestMapper usuarioRestMapper;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        String token = loginUseCase.execute(request.username(), request.password());
        return ResponseEntity.ok(new TokenResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@Valid @RequestBody RegistrarUsuarioRequestDTO request) {
        Usuario nuevoUsuario = usuarioRestMapper.toDomain(request);
        Usuario guardado = registrarUsuarioUseCase.execute(nuevoUsuario, request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioRestMapper.toResponse(guardado));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerPerfilActual(Authentication authentication) {
        Usuario usuario = consultarUsuarioUseCase.obtenerPorUsername(authentication.getName());
        return ResponseEntity.ok(usuarioRestMapper.toResponse(usuario));
    }

    @PutMapping("/me/contrasena")
    public ResponseEntity<MensajeResponseDTO> cambiarContrasenaPropia(
            Authentication authentication,
            @Valid @RequestBody CambiarContrasenaRequestDTO request) {
        gestionarContrasenaUseCase.cambiarContrasenaPropia(
                authentication.getName(),
                request.contrasenaActual(),
                request.nuevaContrasena()
        );
        return ResponseEntity.ok(new MensajeResponseDTO("Contraseña actualizada exitosamente"));
    }

    @PostMapping("/usuarios/{id}/restablecer-contrasena")
    public ResponseEntity<RestablecerContrasenaResponseDTO> restablecerContrasena(@PathVariable Integer id) {
        String contrasenaTemporal = gestionarContrasenaUseCase.restablecerContrasenaPorAdmin(id);
        return ResponseEntity.ok(new RestablecerContrasenaResponseDTO(
                id,
                "Contraseña restablecida y notificada al correo del usuario",
                contrasenaTemporal
        ));
    }

    @PatchMapping("/usuarios/{id}/estado")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstadoUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody CambiarEstadoUsuarioRequestDTO request,
            Authentication authentication) {
        String solicitante = authentication != null ? authentication.getName() : null;
        Usuario actualizado = cambiarEstadoUsuarioUseCase.execute(id, request.activo(), solicitante);
        return ResponseEntity.ok(usuarioRestMapper.toResponse(actualizado));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        List<UsuarioResponseDTO> respuesta = consultarUsuarioUseCase.listarUsuarios()
                .stream()
                .map(usuarioRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioPorId(@PathVariable Integer id) {
        Usuario usuario = consultarUsuarioUseCase.obtenerPorId(id);
        return ResponseEntity.ok(usuarioRestMapper.toResponse(usuario));
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarUsuarioRequestDTO request) {
        Usuario datos = usuarioRestMapper.toDomain(request);
        Usuario actualizado = actualizarUsuarioUseCase.execute(id, datos);
        return ResponseEntity.ok(usuarioRestMapper.toResponse(actualizado));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RolResponseDTO>> listarRoles() {
        List<RolResponseDTO> roles = consultarUsuarioUseCase.listarRoles()
                .stream()
                .map(usuarioRestMapper::toRolResponse)
                .toList();
        return ResponseEntity.ok(roles);
    }
}
