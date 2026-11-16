package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.gateway.MetodoPagoGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MetodoPagoUseCase {

    private final MetodoPagoGateway metodoPagoGateway;

    public MetodoPago obtenerPorId(Integer idMetodoPago) {
        if (idMetodoPago == null) {
            throw new ReglaNegocioException("El ID del método de pago es obligatorio");
        }
        return metodoPagoGateway.findById(idMetodoPago)
                .orElseThrow(() -> new RecursoNoEncontradoException("Método de pago no encontrado con ID: " + idMetodoPago));
    }

    public List<MetodoPago> listarTodos() {
        return metodoPagoGateway.findAll();
    }
}
