package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.gateway.TipoMovimientoGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TipoMovimientoUseCase {

    private final TipoMovimientoGateway tipoMovimientoGateway;

    public TipoMovimiento obtenerPorId(Integer idMovimiento) {
        if (idMovimiento == null) {
            throw new ReglaNegocioException("El ID del tipo de movimiento es obligatorio");
        }
        return tipoMovimientoGateway.findById(idMovimiento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de movimiento no encontrado con ID: " + idMovimiento));
    }

    public List<TipoMovimiento> listarTodos() {
        return tipoMovimientoGateway.findAll();
    }
}
