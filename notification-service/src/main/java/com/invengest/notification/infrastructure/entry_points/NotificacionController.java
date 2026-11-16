package com.invengest.notification.infrastructure.entry_points;

import com.invengest.notification.domain.usecase.EnviarNotificacionUseCase;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionAlertaStockRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionBienvenidaRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionCierreCajaRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionComprobanteVentaRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionEncoladaResponseDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionPruebaRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionSeguridadRequestDTO;
import com.invengest.notification.infrastructure.mapper.NotificacionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificacionController {

    private final EnviarNotificacionUseCase enviarNotificacionUseCase;
    private final NotificacionMapper notificacionMapper;

    @PostMapping("/internal/notificaciones/bienvenida")
    public ResponseEntity<NotificacionEncoladaResponseDTO> enviarBienvenida(
            @Valid @RequestBody NotificacionBienvenidaRequestDTO request) {
        String idSolicitud = enviarNotificacionUseCase.enviarBienvenida(notificacionMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(NotificacionEncoladaResponseDTO.encolada(idSolicitud));
    }

    @PostMapping("/internal/notificaciones/seguridad-cuenta")
    public ResponseEntity<NotificacionEncoladaResponseDTO> enviarSeguridadCuenta(
            @Valid @RequestBody NotificacionSeguridadRequestDTO request) {
        String idSolicitud = enviarNotificacionUseCase.enviarSeguridadCuenta(notificacionMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(NotificacionEncoladaResponseDTO.encolada(idSolicitud));
    }

    @PostMapping("/internal/notificaciones/comprobante-venta")
    public ResponseEntity<NotificacionEncoladaResponseDTO> enviarComprobanteVenta(
            @Valid @RequestBody NotificacionComprobanteVentaRequestDTO request) {
        String idSolicitud = enviarNotificacionUseCase.enviarComprobanteVenta(notificacionMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(NotificacionEncoladaResponseDTO.encolada(idSolicitud));
    }

    @PostMapping("/internal/notificaciones/alerta-stock")
    public ResponseEntity<NotificacionEncoladaResponseDTO> enviarAlertaStock(
            @Valid @RequestBody NotificacionAlertaStockRequestDTO request) {
        String idSolicitud = enviarNotificacionUseCase.enviarAlertaStock(notificacionMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(NotificacionEncoladaResponseDTO.encolada(idSolicitud));
    }

    @PostMapping("/internal/notificaciones/cierre-caja")
    public ResponseEntity<NotificacionEncoladaResponseDTO> enviarCierreCaja(
            @Valid @RequestBody NotificacionCierreCajaRequestDTO request) {
        String idSolicitud = enviarNotificacionUseCase.enviarCierreCaja(notificacionMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(NotificacionEncoladaResponseDTO.encolada(idSolicitud));
    }

    @PostMapping("/api/notificaciones/prueba")
    public ResponseEntity<NotificacionEncoladaResponseDTO> enviarPrueba(
            @Valid @RequestBody NotificacionPruebaRequestDTO request) {
        String idSolicitud = enviarNotificacionUseCase.enviarPrueba(request.destinatario());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(NotificacionEncoladaResponseDTO.encolada(idSolicitud));
    }
}
