package com.invengest.transaction.infrastructure.entry_points.rest;

import com.invengest.transaction.domain.model.ComprobanteVenta;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.usecase.AnularVentaUseCase;
import com.invengest.transaction.domain.usecase.ComprobanteVentaUseCase;
import com.invengest.transaction.domain.usecase.ConsultarVentaUseCase;
import com.invengest.transaction.domain.usecase.RegistrarVentaUseCase;
import com.invengest.transaction.infrastructure.entry_points.dto.request.AnularVentaRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.request.EnviarComprobanteRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.request.RegistrarVentaRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ComprobanteVentaResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.VentaResponse;
import com.invengest.transaction.infrastructure.mapper.VentaRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/transactions/ventas", "/api/transaction/ventas"})
@RequiredArgsConstructor
public class VentaController {

    private final RegistrarVentaUseCase registrarVentaUseCase;
    private final AnularVentaUseCase anularVentaUseCase;
    private final ConsultarVentaUseCase consultarVentaUseCase;
    private final ComprobanteVentaUseCase comprobanteVentaUseCase;
    private final VentaRestMapper ventaRestMapper;

    @PostMapping
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody RegistrarVentaRequest request) {
        Venta venta = ventaRestMapper.toDomain(request);
        Venta ventaRegistrada = registrarVentaUseCase.registrarVenta(venta);
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaRestMapper.toResponse(ventaRegistrada));
    }

    @PostMapping("/{id}/anular")
    public ResponseEntity<VentaResponse> anular(
            @PathVariable Integer id,
            @Valid @RequestBody AnularVentaRequest request) {
        Venta ventaAnulada = anularVentaUseCase.anularVenta(
                id,
                request.idUsuarioAnulacion(),
                request.motivoAnulacion()
        );
        return ResponseEntity.ok(ventaRestMapper.toResponse(ventaAnulada));
    }

    @GetMapping("/{id}/comprobante")
    public ResponseEntity<ComprobanteVentaResponse> obtenerComprobante(@PathVariable Integer id) {
        ComprobanteVenta comprobante = comprobanteVentaUseCase.obtenerComprobante(id);
        return ResponseEntity.ok(ventaRestMapper.toComprobanteResponse(comprobante));
    }

    @PostMapping("/{id}/comprobante/enviar")
    public ResponseEntity<ComprobanteVentaResponse> enviarComprobante(
            @PathVariable Integer id,
            @Valid @RequestBody(required = false) EnviarComprobanteRequest request) {
        String emailDestino = request != null ? request.emailDestino() : null;
        ComprobanteVenta comprobante = comprobanteVentaUseCase.enviarComprobantePorCorreo(id, emailDestino);
        return ResponseEntity.ok(ventaRestMapper.toComprobanteResponse(comprobante));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerPorId(@PathVariable Integer id) {
        Venta venta = consultarVentaUseCase.obtenerPorId(id);
        return ResponseEntity.ok(ventaRestMapper.toResponse(venta));
    }

    @GetMapping
    public ResponseEntity<List<VentaResponse>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(required = false) Integer idUsuario) {

        List<Venta> ventas;
        if (inicio != null && fin != null) {
            ventas = consultarVentaUseCase.filtrarPorFechas(inicio, fin);
        } else if (idUsuario != null) {
            ventas = consultarVentaUseCase.listarPorUsuario(idUsuario);
        } else {
            ventas = consultarVentaUseCase.listarTodas();
        }

        List<VentaResponse> responses = ventas.stream()
                .map(ventaRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
