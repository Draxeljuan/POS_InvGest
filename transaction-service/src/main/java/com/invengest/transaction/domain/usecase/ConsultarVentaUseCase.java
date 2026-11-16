package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ConsultarVentaUseCase {

    private final VentaGateway ventaGateway;

    public Venta obtenerPorId(Integer idVenta) {
        if (idVenta == null) {
            throw new ReglaNegocioException("El ID de la venta es obligatorio");
        }
        return ventaGateway.findById(idVenta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con ID: " + idVenta));
    }

    public List<Venta> listarTodas() {
        return ventaGateway.findAll();
    }

    public List<Venta> filtrarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new ReglaNegocioException("Las fechas inicial y final son obligatorias");
        }
        if (fin.isBefore(inicio)) {
            throw new ReglaNegocioException("La fecha final no puede ser anterior a la fecha inicial");
        }
        return ventaGateway.findByFechaBetween(inicio, fin);
    }

    public List<Venta> listarPorUsuario(Integer idUsuario) {
        if (idUsuario == null) {
            throw new ReglaNegocioException("El ID del usuario es obligatorio");
        }
        return ventaGateway.findByIdUsuario(idUsuario);
    }
}
