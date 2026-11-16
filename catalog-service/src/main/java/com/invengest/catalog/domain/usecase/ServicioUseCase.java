package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.exception.ReglaNegocioException;
import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.model.Servicio;
import com.invengest.catalog.domain.model.gateway.CategoriaServicioGateway;
import com.invengest.catalog.domain.model.gateway.ServicioGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ServicioUseCase {

    private final ServicioGateway servicioGateway;
    private final CategoriaServicioGateway categoriaServicioGateway;

    public Servicio registrar(Servicio servicio) {
        servicio.validarInvariantes();
        if (servicioGateway.existsByNombre(servicio.getNombre())) {
            throw new EntidadDuplicadaException("Ya existe un servicio con el nombre: " + servicio.getNombre());
        }
        validarYAsignarCategoria(servicio);
        return servicioGateway.save(servicio);
    }

    public Servicio actualizar(Servicio servicio) {
        servicio.validarInvariantes();
        Servicio existente = servicioGateway.findById(servicio.getIdServicio())
                .orElseThrow(() -> new RecursoNoEncontradoException("El servicio a actualizar no existe."));

        if (!existente.getNombre().equalsIgnoreCase(servicio.getNombre())
                && servicioGateway.existsByNombre(servicio.getNombre())) {
            throw new EntidadDuplicadaException("Ya existe un servicio con el nombre: " + servicio.getNombre());
        }
        validarYAsignarCategoria(servicio);
        return servicioGateway.save(servicio);
    }

    public List<Servicio> obtenerTodos() {
        return servicioGateway.findAll();
    }

    public Servicio obtenerPorId(Integer idServicio) {
        return servicioGateway.findById(idServicio)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado con ID: " + idServicio));
    }

    public List<Servicio> obtenerPorCategoria(Integer idCategoriaServicio) {
        return servicioGateway.findByCategoriaServicioId(idCategoriaServicio);
    }

    public void eliminacionLogica(Integer idServicio) {
        Servicio servicioADeshabilitar = servicioGateway.findById(idServicio)
                .orElseThrow(() -> new RecursoNoEncontradoException("El servicio no existe."));

        CategoriaServicio categoriaDeshabilitado = categoriaServicioGateway
                .findByNombre(CategoriaServicio.CATEGORIA_DESHABILITADOS)
                .orElseThrow(() -> new ReglaNegocioException(
                        "La categoría '" + CategoriaServicio.CATEGORIA_DESHABILITADOS + "' no está configurada en el sistema."));

        servicioADeshabilitar.deshabilitar(categoriaDeshabilitado);
        servicioGateway.save(servicioADeshabilitar);
    }

    private void validarYAsignarCategoria(Servicio servicio) {
        if (servicio.getCategoriaServicio() == null || servicio.getCategoriaServicio().getIdCategoriaServicio() == null) {
            throw new RecursoNoEncontradoException("La categoría de servicio asignada no existe.");
        }
        CategoriaServicio categoria = categoriaServicioGateway
                .findById(servicio.getCategoriaServicio().getIdCategoriaServicio())
                .orElseThrow(() -> new RecursoNoEncontradoException("La categoría de servicio asignada no existe."));
        servicio.setCategoriaServicio(categoria);
    }
}
