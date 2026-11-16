package com.invengest.catalog.infrastructure.entry_points.rest;

import com.invengest.catalog.domain.model.Servicio;
import com.invengest.catalog.domain.usecase.ServicioUseCase;
import com.invengest.catalog.infrastructure.entry_points.dto.request.ServicioRequest;
import com.invengest.catalog.infrastructure.entry_points.dto.response.ServicioResponse;
import com.invengest.catalog.infrastructure.mapper.ServicioRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioUseCase servicioUseCase;
    private final ServicioRestMapper servicioRestMapper;

    @GetMapping
    public ResponseEntity<List<ServicioResponse>> listarTodos() {
        List<ServicioResponse> responses = servicioUseCase.obtenerTodos().stream()
                .map(servicioRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponse> obtenerPorId(@PathVariable Integer id) {
        Servicio servicio = servicioUseCase.obtenerPorId(id);
        return ResponseEntity.ok(servicioRestMapper.toResponse(servicio));
    }

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<ServicioResponse>> obtenerPorCategoria(@PathVariable Integer idCategoria) {
        List<ServicioResponse> responses = servicioUseCase.obtenerPorCategoria(idCategoria).stream()
                .map(servicioRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ServicioResponse> registrar(@Valid @RequestBody ServicioRequest request) {
        Servicio servicio = servicioRestMapper.toDomain(request);
        Servicio registrado = servicioUseCase.registrar(servicio);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioRestMapper.toResponse(registrado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ServicioRequest request) {
        Servicio servicio = servicioRestMapper.toDomain(request);
        servicio.setIdServicio(id);
        Servicio actualizado = servicioUseCase.actualizar(servicio);
        return ResponseEntity.ok(servicioRestMapper.toResponse(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        servicioUseCase.eliminacionLogica(id);
        return ResponseEntity.noContent().build();
    }
}
