package com.invengest.catalog.infrastructure.entry_points.advice;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.exception.ReglaNegocioException;
import com.invengest.catalog.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.catalog.domain.exception.StockInsuficienteException;
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
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("Mapea todas las excepciones de dominio y de Spring MVC a respuestas HTTP estructuradas")
    void mapeaTodasLasExcepcionesCorrectamente() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "nombre", "El nombre es obligatorio"));
        MethodArgumentNotValidException methodArgEx = new MethodArgumentNotValidException(null, bindingResult);

        HandlerMethodValidationException handlerValidationEx = mock(HandlerMethodValidationException.class);
        doReturn(List.of(new FieldError("req", "cantidad", "Cantidad inválida")))
                .when(handlerValidationEx).getAllErrors();

        NoResourceFoundException noResourceEx = new NoResourceFoundException(HttpMethod.GET, "api/catalog/no-existe", "api/catalog/no-existe");
        HttpRequestMethodNotSupportedException methodNotSupportedEx = new HttpRequestMethodNotSupportedException("PATCH");

        // Act
        ResponseEntity<ErrorResponseDTO> r404 = handler.handleNotFound(new RecursoNoEncontradoException("No encontrado"));
        ResponseEntity<ErrorResponseDTO> rNoRes = handler.handleNoResourceFound(noResourceEx);
        ResponseEntity<ErrorResponseDTO> r405 = handler.handleMethodNotSupported(methodNotSupportedEx);
        ResponseEntity<ErrorResponseDTO> r409 = handler.handleConflict(new EntidadDuplicadaException("Duplicado"));
        ResponseEntity<ErrorResponseDTO> rStock = handler.handleInsufficientStock(new StockInsuficienteException("Stock insuficiente"));
        ResponseEntity<ErrorResponseDTO> r400 = handler.handleBusinessRule(new ReglaNegocioException("Regla"));
        ResponseEntity<ErrorResponseDTO> r503 = handler.handleExternalServiceUnavailable(new ServicioExternoNoDisponibleException("DolarAPI caída"));
        ResponseEntity<ErrorResponseDTO> rVal = handler.handleValidationExceptions(methodArgEx);
        ResponseEntity<ErrorResponseDTO> rMethodVal = handler.handleMethodValidationExceptions(handlerValidationEx);
        ResponseEntity<ErrorResponseDTO> rDb = handler.handleDatabaseConstraints(new DataIntegrityViolationException("FK"));
        ResponseEntity<ErrorResponseDTO> r500 = handler.handleGenericException(new RuntimeException("Fallo"));

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, r404.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, rNoRes.getStatusCode());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, r405.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, r409.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rStock.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, r400.getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, r503.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rVal.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rMethodVal.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rDb.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r500.getStatusCode());
        assertNotNull(rNoRes.getBody());
        assertNotNull(r405.getBody());
    }
}
