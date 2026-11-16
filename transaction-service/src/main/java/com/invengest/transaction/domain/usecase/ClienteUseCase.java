package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.EntidadDuplicadaException;
import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.domain.model.gateway.ClienteGateway;
import com.invengest.transaction.domain.model.gateway.TipoDocumentoGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ClienteUseCase {

    private final ClienteGateway clienteGateway;
    private final TipoDocumentoGateway tipoDocumentoGateway;

    public Cliente registrar(Cliente cliente) {
        if (cliente == null) {
            throw new ReglaNegocioException("Los datos del cliente son obligatorios");
        }
        if (cliente.getPrimerNombre() == null || cliente.getPrimerNombre().isBlank()) {
            throw new ReglaNegocioException("El primer nombre es obligatorio");
        }
        if (cliente.getPrimerApellido() == null || cliente.getPrimerApellido().isBlank()) {
            throw new ReglaNegocioException("El primer apellido es obligatorio");
        }

        if (cliente.getDocumentoIdentidad() != null && !cliente.getDocumentoIdentidad().isBlank()) {
            clienteGateway.findByDocumentoIdentidad(cliente.getDocumentoIdentidad().trim()).ifPresent(c -> {
                throw new EntidadDuplicadaException("Ya existe un cliente con el documento: " + cliente.getDocumentoIdentidad());
            });
        }

        if (cliente.getTipoDocumento() != null && cliente.getTipoDocumento().getIdTipoDocumento() != null) {
            Integer idTipoDoc = cliente.getTipoDocumento().getIdTipoDocumento();
            TipoDocumento tipoDoc = tipoDocumentoGateway.findById(idTipoDoc)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de documento no encontrado con ID: " + idTipoDoc));
            cliente.setTipoDocumento(tipoDoc);
        }

        return clienteGateway.save(cliente);
    }

    public Cliente obtenerPorId(Integer idCliente) {
        if (idCliente == null) {
            throw new ReglaNegocioException("El ID del cliente es obligatorio");
        }
        return clienteGateway.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con ID: " + idCliente));
    }

    public Cliente obtenerPorDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new ReglaNegocioException("El documento de identidad es obligatorio");
        }
        return clienteGateway.findByDocumentoIdentidad(documento.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con documento: " + documento));
    }

    public List<Cliente> listarTodos() {
        return clienteGateway.findAll();
    }
}
