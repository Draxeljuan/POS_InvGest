package com.invengest.auth.infrastructure.entry_points;

import com.invengest.auth.domain.model.Usuario;
import com.invengest.auth.domain.usecase.ConsultarUsuarioUseCase;
import com.invengest.auth.infrastructure.entry_points.dto.ContactoUsuarioDTO;
import com.invengest.auth.infrastructure.mapper.UsuarioRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/usuarios")
@RequiredArgsConstructor
public class InternalUsuarioController {

    private final ConsultarUsuarioUseCase consultarUsuarioUseCase;
    private final UsuarioRestMapper usuarioRestMapper;

    @GetMapping("/{id}/contacto")
    public ResponseEntity<ContactoUsuarioDTO> obtenerContactoUsuario(@PathVariable Integer id) {
        Usuario usuario = consultarUsuarioUseCase.obtenerPorId(id);
        return ResponseEntity.ok(usuarioRestMapper.toContactoResponse(usuario));
    }

    @GetMapping("/administradores/contacto")
    public ResponseEntity<List<ContactoUsuarioDTO>> obtenerContactosAdministradores() {
        List<ContactoUsuarioDTO> contactos = consultarUsuarioUseCase.listarAdministradoresActivos()
                .stream()
                .map(usuarioRestMapper::toContactoResponse)
                .toList();
        return ResponseEntity.ok(contactos);
    }
}
