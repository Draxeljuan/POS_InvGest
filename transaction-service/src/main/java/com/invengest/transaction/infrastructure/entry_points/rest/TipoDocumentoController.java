package com.invengest.transaction.infrastructure.entry_points.rest;

import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.domain.usecase.TipoDocumentoUseCase;
import com.invengest.transaction.infrastructure.entry_points.dto.response.TipoDocumentoResponse;
import com.invengest.transaction.infrastructure.mapper.TipoDocumentoRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/transactions/tipos-documento", "/api/transaction/tipos-documento"})
@RequiredArgsConstructor
public class TipoDocumentoController {

    private final TipoDocumentoUseCase tipoDocumentoUseCase;
    private final TipoDocumentoRestMapper mapper;

    @GetMapping
    public ResponseEntity<List<TipoDocumentoResponse>> listarTodos() {
        List<TipoDocumentoResponse> responses = tipoDocumentoUseCase.listarTodos().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoDocumentoResponse> obtenerPorId(@PathVariable Integer id) {
        TipoDocumento tipoDocumento = tipoDocumentoUseCase.obtenerPorId(id);
        return ResponseEntity.ok(mapper.toResponse(tipoDocumento));
    }
}
