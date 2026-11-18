package com.invengest.auth.infrastructure.entry_points.advice;

import com.invengest.auth.domain.exception.CredencialesInvalidasException;
import com.invengest.auth.domain.exception.RecursoNoEncontradoException;
import com.invengest.auth.domain.exception.ReglaNegocioException;
import com.invengest.auth.domain.exception.UsuarioYaExisteException;
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
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("Debe mapear todas las excepciones de dominio y técnicas a respuestas HTTP estándar")
    void handlers_DebenMapearExcepcionesACodigosHttp() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "req");
        bindingResult.addError(new FieldError("req", "username", "Obligatorio"));
        MethodArgumentNotValidException validEx = new MethodArgumentNotValidException(null, bindingResult);
        NoResourceFoundException notFoundRouteEx = new NoResourceFoundException(HttpMethod.GET, "api/auth/no-existe", "api/auth/no-existe");
        HttpRequestMethodNotSupportedException methodNotSupportedEx = new HttpRequestMethodNotSupportedException("DELETE");

        // Act
        ResponseEntity<ErrorResponseDTO> r401 = handler.handleInvalidCredentials(new CredencialesInvalidasException("Credenciales"));
        ResponseEntity<ErrorResponseDTO> r404 = handler.handleNotFound(new RecursoNoEncontradoException("No existe"));
        ResponseEntity<ErrorResponseDTO> r404Route = handler.handleNoResourceFound(notFoundRouteEx);
        ResponseEntity<ErrorResponseDTO> r405 = handler.handleMethodNotSupported(methodNotSupportedEx);
        ResponseEntity<ErrorResponseDTO> r409 = handler.handleUserAlreadyExists(new UsuarioYaExisteException("Duplicado"));
        ResponseEntity<ErrorResponseDTO> r400 = handler.handleBusinessRule(new ReglaNegocioException("Regla"));
        ResponseEntity<ErrorResponseDTO> rValid = handler.handleValidationExceptions(validEx);
        ResponseEntity<ErrorResponseDTO> rDb = handler.handleDatabaseConstraints(new DataIntegrityViolationException("FK"));
        ResponseEntity<ErrorResponseDTO> r500 = handler.handleGenericException(new RuntimeException("Fallo"));

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, r401.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, r404.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, r404Route.getStatusCode());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, r405.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, r409.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, r400.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rValid.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rDb.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r500.getStatusCode());
    }
}
