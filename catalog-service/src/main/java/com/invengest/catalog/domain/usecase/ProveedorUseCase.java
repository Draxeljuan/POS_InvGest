package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.EntidadEnUsoException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.exception.ReglaNegocioException;
import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.Proveedor;
import com.invengest.catalog.domain.model.gateway.EstadoProveedorGateway;
import com.invengest.catalog.domain.model.gateway.ProveedorGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ProveedorUseCase {

    private final ProveedorGateway proveedorGateway;
    private final EstadoProveedorGateway estadoProveedorGateway;

    public Proveedor crear(Proveedor proveedor) {
        proveedor.validarInvariantes();
        if (proveedor.getNit() != null && !proveedor.getNit().isBlank()
                && proveedorGateway.findByNit(proveedor.getNit()).isPresent()) {
            throw new EntidadDuplicadaException("Ya existe un proveedor registrado con el NIT: " + proveedor.getNit());
        }
        resolverEstadoProveedor(proveedor);
        return proveedorGateway.save(proveedor);
    }

    public Proveedor actualizar(Proveedor proveedor) {
        proveedor.validarInvariantes();
        if (proveedor.getIdProveedor() == null || proveedorGateway.findById(proveedor.getIdProveedor()).isEmpty()) {
            throw new RecursoNoEncontradoException("El proveedor a actualizar no existe.");
        }
        resolverEstadoProveedor(proveedor);
        return proveedorGateway.save(proveedor);
    }

    public List<Proveedor> obtenerTodos() {
        return proveedorGateway.findAll();
    }

    public Proveedor obtenerPorId(Integer id) {
        return proveedorGateway.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con ID: " + id));
    }

    public void eliminacionLogica(Integer id) {
        Proveedor proveedorADescontinuar = proveedorGateway.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El proveedor no existe."));

        if (proveedorGateway.existsProductosByProveedorId(id)) {
            throw new EntidadEnUsoException("No se puede dar de baja el proveedor porque tiene productos asociados.");
        }

        EstadoProveedor estadoInactivo = estadoProveedorGateway.findByNombre(EstadoProveedor.ESTADO_INACTIVO)
                .orElseThrow(() -> new ReglaNegocioException(
                        "El estado '" + EstadoProveedor.ESTADO_INACTIVO + "' no está configurado en el sistema de proveedores."));

        proveedorADescontinuar.desactivar(estadoInactivo);
        proveedorGateway.save(proveedorADescontinuar);
    }

    private void resolverEstadoProveedor(Proveedor proveedor) {
        if (proveedor.getEstado() != null && proveedor.getEstado().getIdEstadoProveedor() != null) {
            EstadoProveedor estado = estadoProveedorGateway.findById(proveedor.getEstado().getIdEstadoProveedor())
                    .orElseThrow(() -> new RecursoNoEncontradoException("El estado del proveedor asignado no existe."));
            proveedor.setEstado(estado);
        } else {
            EstadoProveedor estadoActivo = estadoProveedorGateway.findByNombre(EstadoProveedor.ESTADO_ACTIVO)
                    .orElseThrow(() -> new ReglaNegocioException(
                            "El estado '" + EstadoProveedor.ESTADO_ACTIVO + "' no está configurado en el sistema de proveedores."));
            proveedor.setEstado(estadoActivo);
        }
    }
}
