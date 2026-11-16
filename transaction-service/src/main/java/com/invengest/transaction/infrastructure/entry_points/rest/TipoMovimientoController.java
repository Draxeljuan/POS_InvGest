package com.invengest.transaction.infrastructure.entry_points.rest;

import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.usecase.TipoMovimientoUseCase;
import com.invengest.transaction.infrastructure.entry_points.dto.response.TipoMovimientoResponse;
import com.invengest.transaction.infrastructure.mapper.TipoMovimientoRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/transactions/tipos-movimiento", "/api/transaction/tipos-movimiento"})
@RequiredArgsConstructor
public class TipoMovimientoController {

    private final TipoMovimientoUseCase tipoMovimientoUseCase;
    private final TipoMovimientoRestMapper mapper;

    @GetMapping
    public ResponseEntity<List<TipoMovimientoResponse>> listarTodos() {
        List<TipoMovimientoResponse> responses = tipoMovimientoUseCase.listarTodos().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoMovimientoResponse> obtenerPorId(@PathVariable Integer id) {
        TipoMovimiento tipoMovimiento = tipoMovimientoUseCase.obtenerPorId(id);
        return ResponseEntity.ok(mapper.toResponse(tipoMovimiento));
    }
}
