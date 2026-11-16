package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.domain.model.gateway.TipoDocumentoGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TipoDocumentoUseCase {

    private final TipoDocumentoGateway tipoDocumentoGateway;

    public TipoDocumento obtenerPorId(Integer idTipoDocumento) {
        if (idTipoDocumento == null) {
            throw new ReglaNegocioException("El ID del tipo de documento es obligatorio");
        }
        return tipoDocumentoGateway.findById(idTipoDocumento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de documento no encontrado con ID: " + idTipoDocumento));
    }

    public List<TipoDocumento> listarTodos() {
        return tipoDocumentoGateway.findAll();
    }
}
