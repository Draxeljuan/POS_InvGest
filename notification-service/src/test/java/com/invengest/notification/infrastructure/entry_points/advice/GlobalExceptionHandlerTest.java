package com.invengest.notification.infrastructure.entry_points.advice;

import com.invengest.notification.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Debe mapear ReglaNegocioException, validaciones, JSON inválido, 404, 405 y 500")
    void handlers_DebenMapearTodosLosCodigosHttpEsperados() {
        // Arrange
        ReglaNegocioException reglaEx = new ReglaNegocioException("Evento inválido");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "destinatario", "El correo es obligatorio"));
        MethodArgumentNotValidException validEx = new MethodArgumentNotValidException(null, bindingResult);
        HttpMessageNotReadableException jsonEx = new HttpMessageNotReadableException("JSON malformado", (Throwable) null, null);
        NoResourceFoundException notFoundEx = new NoResourceFoundException(HttpMethod.POST, "api/notifications/prueba", "api/notifications/prueba");
        HttpRequestMethodNotSupportedException methodNotSupportedEx = new HttpRequestMethodNotSupportedException("GET");
        Exception genericEx = new RuntimeException("Fallo interno");

        // Act
        ResponseEntity<ErrorResponseDTO> respRegla = exceptionHandler.handleBusinessRule(reglaEx);
        ResponseEntity<ErrorResponseDTO> respValid = exceptionHandler.handleValidationExceptions(validEx);
        ResponseEntity<ErrorResponseDTO> respJson = exceptionHandler.handleNotReadable(jsonEx);
        ResponseEntity<ErrorResponseDTO> resp404 = exceptionHandler.handleNoResourceFound(notFoundEx);
        ResponseEntity<ErrorResponseDTO> resp405 = exceptionHandler.handleMethodNotSupported(methodNotSupportedEx);
        ResponseEntity<ErrorResponseDTO> respGeneric = exceptionHandler.handleGenericException(genericEx);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respRegla.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, respValid.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, respJson.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, resp404.getStatusCode());
        assertNotNull(resp404.getBody());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, resp405.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respGeneric.getStatusCode());
    }
}
