package com.invengest.notification.infrastructure.entry_points;

import com.invengest.notification.domain.exception.ReglaNegocioException;
import com.invengest.notification.domain.usecase.EnviarNotificacionUseCase;
import com.invengest.notification.infrastructure.entry_points.advice.ErrorResponseDTO;
import com.invengest.notification.infrastructure.entry_points.advice.GlobalExceptionHandler;
import com.invengest.notification.infrastructure.entry_points.dto.ItemAlertaStockDTO;
import com.invengest.notification.infrastructure.entry_points.dto.ItemComprobanteDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionAlertaStockRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionBienvenidaRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionCierreCajaRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionComprobanteVentaRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionEncoladaResponseDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionPruebaRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.NotificacionSeguridadRequestDTO;
import com.invengest.notification.infrastructure.entry_points.dto.TotalMetodoPagoDTO;
import com.invengest.notification.infrastructure.mapper.NotificacionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock
    private EnviarNotificacionUseCase enviarNotificacionUseCase;

    private NotificacionController controller;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        NotificacionMapper mapper = Mappers.getMapper(NotificacionMapper.class);
        controller = new NotificacionController(enviarNotificacionUseCase, mapper);
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("POST /internal/notificaciones/bienvenida responde 202 Accepted con estado ENCOLADA")
    void enviarBienvenida_Retorna202Accepted() {
        // Arrange
        NotificacionBienvenidaRequestDTO request = new NotificacionBienvenidaRequestDTO(
                "nuevo@papeleria.local", "Pedro Pérez", "pperez", "Vendedor"
        );
        when(enviarNotificacionUseCase.enviarBienvenida(any())).thenReturn("req-bienvenida-01");

        // Act
        ResponseEntity<NotificacionEncoladaResponseDTO> response = controller.enviarBienvenida(request);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("req-bienvenida-01", response.getBody().idSolicitud());
        assertEquals("ENCOLADA", response.getBody().estado());
    }

    @Test
    @DisplayName("POST /internal/notificaciones/seguridad-cuenta responde 202 Accepted")
    void enviarSeguridadCuenta_Retorna202Accepted() {
        // Arrange
        NotificacionSeguridadRequestDTO request = new NotificacionSeguridadRequestDTO(
                "user@papeleria.local", "Pedro Pérez", "CAMBIO_CONTRASENA", null
        );
        when(enviarNotificacionUseCase.enviarSeguridadCuenta(any())).thenReturn("req-seg-02");

        // Act
        ResponseEntity<NotificacionEncoladaResponseDTO> response = controller.enviarSeguridadCuenta(request);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("req-seg-02", response.getBody().idSolicitud());
        assertEquals("ENCOLADA", response.getBody().estado());
    }

    @Test
    @DisplayName("POST /internal/notificaciones/comprobante-venta responde 202 Accepted")
    void enviarComprobanteVenta_Retorna202Accepted() {
        // Arrange
        NotificacionComprobanteVentaRequestDTO request = new NotificacionComprobanteVentaRequestDTO(
                "cliente@correo.com",
                "Ana López",
                50,
                "2026-11-16",
                List.of(new ItemComprobanteDTO("Carpeta", 2, new BigDecimal("3000"), new BigDecimal("6000"))),
                new BigDecimal("6000"),
                "Transferencia"
        );
        when(enviarNotificacionUseCase.enviarComprobanteVenta(any())).thenReturn("req-venta-03");

        // Act
        ResponseEntity<NotificacionEncoladaResponseDTO> response = controller.enviarComprobanteVenta(request);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("req-venta-03", response.getBody().idSolicitud());
        assertEquals("ENCOLADA", response.getBody().estado());
    }

    @Test
    @DisplayName("POST /internal/notificaciones/alerta-stock responde 202 Accepted")
    void enviarAlertaStock_Retorna202Accepted() {
        // Arrange
        NotificacionAlertaStockRequestDTO request = new NotificacionAlertaStockRequestDTO(
                List.of("admin@papeleria.local"),
                List.of(new ItemAlertaStockDTO("PROD-01", "Tijeras", 1, 5, "Bajo Stock"))
        );
        when(enviarNotificacionUseCase.enviarAlertaStock(any())).thenReturn("req-stock-04");

        // Act
        ResponseEntity<NotificacionEncoladaResponseDTO> response = controller.enviarAlertaStock(request);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("req-stock-04", response.getBody().idSolicitud());
    }

    @Test
    @DisplayName("POST /internal/notificaciones/cierre-caja responde 202 Accepted")
    void enviarCierreCaja_Retorna202Accepted() {
        // Arrange
        NotificacionCierreCajaRequestDTO request = new NotificacionCierreCajaRequestDTO(
                "admin@papeleria.local",
                "2026-11-16",
                List.of(new TotalMetodoPagoDTO("Efectivo", new BigDecimal("200000"))),
                new BigDecimal("200000"),
                new BigDecimal("200000"),
                BigDecimal.ZERO,
                true,
                "2026-11-17"
        );
        when(enviarNotificacionUseCase.enviarCierreCaja(any())).thenReturn("req-caja-05");

        // Act
        ResponseEntity<NotificacionEncoladaResponseDTO> response = controller.enviarCierreCaja(request);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("req-caja-05", response.getBody().idSolicitud());
    }

    @Test
    @DisplayName("POST /api/notificaciones/prueba responde 202 Accepted")
    void enviarPrueba_Retorna202Accepted() {
        // Arrange
        NotificacionPruebaRequestDTO request = new NotificacionPruebaRequestDTO("admin@papeleria.local");
        when(enviarNotificacionUseCase.enviarPrueba("admin@papeleria.local")).thenReturn("req-test-06");

        // Act
        ResponseEntity<NotificacionEncoladaResponseDTO> response = controller.enviarPrueba(request);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("req-test-06", response.getBody().idSolicitud());
    }

    @Test
    @DisplayName("GlobalExceptionHandler mapea errores de negocio, validación, JSON malformado y genéricos")
    void globalExceptionHandler_MapeaCodigosHttp() {
        // Arrange
        ReglaNegocioException reglaEx = new ReglaNegocioException("Evento inválido");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "destinatario", "El correo es obligatorio"));
        MethodArgumentNotValidException validEx = new MethodArgumentNotValidException(null, bindingResult);
        HttpMessageNotReadableException jsonEx = new HttpMessageNotReadableException("JSON malformado", (Throwable) null, null);
        Exception genericEx = new RuntimeException("Fallo interno");

        // Act
        ResponseEntity<ErrorResponseDTO> respRegla = exceptionHandler.handleBusinessRule(reglaEx);
        ResponseEntity<ErrorResponseDTO> respValid = exceptionHandler.handleValidationExceptions(validEx);
        ResponseEntity<ErrorResponseDTO> respJson = exceptionHandler.handleNotReadable(jsonEx);
        ResponseEntity<ErrorResponseDTO> respGeneric = exceptionHandler.handleGenericException(genericEx);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respRegla.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, respValid.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, respJson.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respGeneric.getStatusCode());
    }
}
