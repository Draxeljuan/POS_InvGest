package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.EntidadDuplicadaException;
import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.transaction.domain.model.CalendarioLaboral;
import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.CierreCajaDetalle;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.ResumenCaja;
import com.invengest.transaction.domain.model.ResumenMetodoPago;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.CierreCajaGateway;
import com.invengest.transaction.domain.model.gateway.NotificacionGateway;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CajaUseCase {

    private final VentaGateway ventaGateway;
    private final CierreCajaGateway cierreCajaGateway;
    private final CalendarioLaboral calendarioLaboral;
    private final NotificacionGateway notificacionGateway;

    public ResumenCaja obtenerResumenCaja(LocalDate fechaConsulta) {
        LocalDate fecha = fechaConsulta != null ? fechaConsulta : LocalDate.now();
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        List<Venta> ventasDelDia = ventaGateway.findByFechaBetween(inicio, fin);

        BigDecimal totalCompletadas = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int cantidadCompletadas = 0;
        BigDecimal totalAnuladas = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int cantidadAnuladas = 0;
        BigDecimal efectivoEsperado = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        Map<Integer, ResumenMetodoPago> acumuladoPorMetodo = new LinkedHashMap<>();

        if (ventasDelDia != null) {
            for (Venta venta : ventasDelDia) {
                BigDecimal monto = venta.getTotal() != null ? venta.getTotal() : BigDecimal.ZERO;
                if (venta.estaAnulada()) {
                    totalAnuladas = totalAnuladas.add(monto).setScale(2, RoundingMode.HALF_UP);
                    cantidadAnuladas++;
                } else {
                    totalCompletadas = totalCompletadas.add(monto).setScale(2, RoundingMode.HALF_UP);
                    cantidadCompletadas++;

                    MetodoPago mp = venta.getMetodoPago();
                    if (mp != null && mp.esEfectivo()) {
                        efectivoEsperado = efectivoEsperado.add(monto).setScale(2, RoundingMode.HALF_UP);
                    }
                    if (mp != null && mp.getIdMetodoPago() != null) {
                        ResumenMetodoPago actual = acumuladoPorMetodo.computeIfAbsent(
                                mp.getIdMetodoPago(),
                                id -> new ResumenMetodoPago(id, mp.getNombre(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), 0)
                        );
                        actual.setTotal(actual.getTotal().add(monto).setScale(2, RoundingMode.HALF_UP));
                        actual.setCantidadOperaciones(actual.getCantidadOperaciones() + 1);
                    }
                }
            }
        }

        boolean cierreRegistrado = cierreCajaGateway.existsByFechaCierre(fecha);

        return new ResumenCaja(
                fecha,
                totalCompletadas,
                cantidadCompletadas,
                totalAnuladas,
                cantidadAnuladas,
                efectivoEsperado,
                new ArrayList<>(acumuladoPorMetodo.values()),
                cierreRegistrado
        );
    }

    public CierreCaja registrarCierreCaja(
            LocalDate fechaCierre,
            Integer idUsuario,
            BigDecimal efectivoContado,
            String observacion) {

        LocalDate fecha = fechaCierre != null ? fechaCierre : LocalDate.now();
        if (idUsuario == null) {
            throw new ReglaNegocioException("El ID del usuario responsable del cierre es obligatorio");
        }
        if (efectivoContado == null || efectivoContado.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El efectivo contado no puede ser nulo o negativo");
        }
        if (cierreCajaGateway.existsByFechaCierre(fecha)) {
            throw new EntidadDuplicadaException("Ya existe un cierre de caja registrado para la fecha: " + fecha);
        }

        ResumenCaja resumen = obtenerResumenCaja(fecha);

        CierreCaja cierre = new CierreCaja();
        cierre.setFechaCierre(fecha);
        cierre.setIdUsuario(idUsuario);
        cierre.setTotalVentas(resumen.getTotalVentasCompletadas());
        cierre.setCantidadVentas(resumen.getCantidadVentasCompletadas());
        cierre.setEfectivoEsperado(resumen.getEfectivoEsperado());
        cierre.setEfectivoContado(efectivoContado);
        cierre.calcularDiferencia();
        cierre.setObservacion(observacion);
        cierre.setFechaRegistro(LocalDateTime.now());

        evaluarCalendarioConDegradacionControlada(cierre, fecha);

        List<CierreCajaDetalle> detalles = resumen.getTotalesPorMetodoPago().stream()
                .map(rm -> new CierreCajaDetalle(
                        null,
                        null,
                        new MetodoPago(rm.getIdMetodoPago(), rm.getNombreMetodoPago()),
                        rm.getTotal(),
                        rm.getCantidadOperaciones()
                ))
                .toList();
        cierre.setDetalles(new ArrayList<>(detalles));

        CierreCaja guardado = cierreCajaGateway.save(cierre);
        if (guardado.getDetalles() == null || guardado.getDetalles().isEmpty()) {
            guardado.setDetalles(cierre.getDetalles());
        }

        try {
            notificacionGateway.enviarResumenCierreCaja(guardado);
        } catch (RuntimeException ignored) {
            // Aislado para no interrumpir el cierre de caja
        }

        return guardado;
    }

    public CierreCaja obtenerCierrePorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new ReglaNegocioException("La fecha de cierre a consultar es obligatoria");
        }
        return cierreCajaGateway.findByFechaCierre(fecha)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe cierre de caja para la fecha: " + fecha));
    }

    public List<CierreCaja> listarCierres() {
        return cierreCajaGateway.findAll();
    }

    private void evaluarCalendarioConDegradacionControlada(CierreCaja cierre, LocalDate fecha) {
        try {
            boolean esHabil = calendarioLaboral.esDiaHabil(fecha);
            LocalDate proximoHabil = calendarioLaboral.siguienteDiaHabil(fecha);
            cierre.registrarCalendario(esHabil, proximoHabil);
        } catch (ServicioExternoNoDisponibleException ex) {
            boolean esHabilFallback = fecha.getDayOfWeek() != DayOfWeek.SUNDAY;
            LocalDate candidato = fecha.plusDays(1);
            if (candidato.getDayOfWeek() == DayOfWeek.SUNDAY) {
                candidato = candidato.plusDays(1);
            }
            cierre.registrarCalendario(esHabilFallback, candidato);
        }
    }
}
