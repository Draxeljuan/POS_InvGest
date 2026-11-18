package com.invengest.catalog.infrastructure.entry_points.advice;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.EntidadEnUsoException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.exception.ReglaNegocioException;
import com.invengest.catalog.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.catalog.domain.exception.StockInsuficienteException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final ZoneId ZONA_BOGOTA = ZoneId.of("America/Bogota");

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(RecursoNoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso No Encontrado", List.of(ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoResourceFound(NoResourceFoundException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Recurso No Encontrado",
                List.of("La ruta solicitada no existe: /" + ex.getResourcePath())
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Método No Permitido",
                List.of("El método HTTP " + ex.getMethod() + " no está soportado para esta ruta")
        );
    }

    @ExceptionHandler({EntidadDuplicadaException.class, EntidadEnUsoException.class})
    public ResponseEntity<ErrorResponseDTO> handleConflict(ReglaNegocioException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflicto de Datos", List.of(ex.getMessage()));
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponseDTO> handleInsufficientStock(StockInsuficienteException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Stock Insuficiente", List.of(ex.getMessage()));
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessRule(ReglaNegocioException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Violación de Regla de Negocio", List.of(ex.getMessage()));
    }

    @ExceptionHandler(ServicioExternoNoDisponibleException.class)
    public ResponseEntity<ErrorResponseDTO> handleExternalServiceUnavailable(ServicioExternoNoDisponibleException ex) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Servicio Externo No Disponible", List.of(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Petición Inválida", errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodValidationExceptions(HandlerMethodValidationException ex) {
        List<String> errors = ex.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Petición Inválida", errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDatabaseConstraints(DataIntegrityViolationException ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Error de Integridad",
                List.of("Datos inválidos o restricción de integridad violada")
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error Interno del Servidor",
                List.of("Ocurrió un error inesperado al procesar la solicitud")
        );
    }

    private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status, String error, List<String> messages) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(ZONA_BOGOTA),
                status.value(),
                error,
                messages
        );
        return ResponseEntity.status(status).body(body);
    }
}
