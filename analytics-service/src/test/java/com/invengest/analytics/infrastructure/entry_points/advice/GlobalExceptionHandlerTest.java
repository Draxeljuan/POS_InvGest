package com.invengest.analytics.infrastructure.entry_points.advice;

import com.invengest.analytics.domain.exception.GeneracionReporteException;
import com.invengest.analytics.domain.exception.RecursoNoEncontradoException;
import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.exception.ServicioExternoNoDisponibleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Debe construir respuestas HTTP estándar para excepciones de dominio, rutas inexistentes (404) y métodos no soportados (405)")
    void handlers_ExcepcionesDominioYHttp() {
        // Arrange
        RecursoNoEncontradoException notFoundEx = new RecursoNoEncontradoException("No encontrado");
        NoResourceFoundException noResourceEx = new NoResourceFoundException(HttpMethod.GET, "/api/analytics/inexistente", "api/analytics/inexistente");
        HttpRequestMethodNotSupportedException methodNotSupportedEx = new HttpRequestMethodNotSupportedException("DELETE");
        ReglaNegocioException reglaEx = new ReglaNegocioException("Mes inválido");
        ServicioExternoNoDisponibleException extEx = new ServicioExternoNoDisponibleException("TRM caída");
        GeneracionReporteException repEx = new GeneracionReporteException("Fallo PDF", new RuntimeException("IO"));

        // Act
        ResponseEntity<ErrorResponseDTO> res404 = handler.handleNotFound(notFoundEx);
        ResponseEntity<ErrorResponseDTO> resNoResource = handler.handleNoResourceFound(noResourceEx);
        ResponseEntity<ErrorResponseDTO> res405 = handler.handleMethodNotSupported(methodNotSupportedEx);
        ResponseEntity<ErrorResponseDTO> res400 = handler.handleBusinessRule(reglaEx);
        ResponseEntity<ErrorResponseDTO> res503 = handler.handleExternalServiceUnavailable(extEx);
        ResponseEntity<ErrorResponseDTO> res500Rep = handler.handleReportGeneration(repEx);

        // Assert
        assertThat(res404.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resNoResource.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resNoResource.getBody().messages().getFirst()).contains("/api/analytics/inexistente");
        assertThat(res405.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(res405.getBody().messages().getFirst()).contains("DELETE");
        assertThat(res400.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res503.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(res500Rep.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Debe manejar errores de validación, parámetros faltantes, integridad de datos y errores genéricos")
    void handlers_ValidacionesYGenericos() {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "req");
        bindingResult.addError(new FieldError("req", "campo", "El campo es obligatorio"));
        MethodArgumentNotValidException validEx = mock(MethodArgumentNotValidException.class);
        when(validEx.getBindingResult()).thenReturn(bindingResult);

        HandlerMethodValidationException methodValidEx = mock(HandlerMethodValidationException.class);
        doReturn(List.of(new FieldError("req", "param", "Parámetro inválido"))).when(methodValidEx).getAllErrors();

        MissingServletRequestParameterException missingParamEx = new MissingServletRequestParameterException("anio", "int");
        DataIntegrityViolationException integrityEx = new DataIntegrityViolationException("Constraint");
        RuntimeException genericEx = new RuntimeException("Fallo inesperado");

        // Act
        ResponseEntity<ErrorResponseDTO> resValid = handler.handleValidationExceptions(validEx);
        ResponseEntity<ErrorResponseDTO> resMethodValid = handler.handleMethodValidationExceptions(methodValidEx);
        ResponseEntity<ErrorResponseDTO> resParam = handler.handleRequestParamExceptions(missingParamEx);
        ResponseEntity<ErrorResponseDTO> resIntegrity = handler.handleDatabaseConstraints(integrityEx);
        ResponseEntity<ErrorResponseDTO> resGeneric = handler.handleGenericException(genericEx);

        // Assert
        assertThat(resValid.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resMethodValid.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resParam.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resIntegrity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resGeneric.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
