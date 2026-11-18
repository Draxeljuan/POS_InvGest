package com.invengest.transaction.infrastructure.entry_points.advice;

import com.invengest.transaction.domain.exception.EntidadDuplicadaException;
import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.transaction.domain.exception.StockInsuficienteException;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("Debe traducir todas las excepciones de dominio, HTTP y validación a respuestas JSON estándar")
    void debeTraducirTodasLasExcepcionesAJsonEstandar() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Request req = Request.create(Request.HttpMethod.GET, "/test", Map.of(), null, StandardCharsets.UTF_8, null);

        MethodArgumentNotValidException validationEx = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(validationEx.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "campo", "Campo obligatorio")));

        HandlerMethodValidationException methodValEx = mock(HandlerMethodValidationException.class);
        MessageSourceResolvable resolvable = mock(MessageSourceResolvable.class);
        when(resolvable.getDefaultMessage()).thenReturn("Parámetro inválido");
        doReturn(List.of(resolvable)).when(methodValEx).getAllErrors();

        // Act
        ResponseEntity<ErrorResponseDTO> r404 = handler.handleNotFound(new RecursoNoEncontradoException("No existe"));
        ResponseEntity<ErrorResponseDTO> rNoRes = handler.handleNoResourceFound(
                new NoResourceFoundException(HttpMethod.GET, "/api/transactions/ruta-inexistente", "api/transactions/ruta-inexistente"));
        ResponseEntity<ErrorResponseDTO> r405 = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("DELETE"));
        ResponseEntity<ErrorResponseDTO> r409 = handler.handleConflict(new EntidadDuplicadaException("Duplicado"));
        ResponseEntity<ErrorResponseDTO> rStock = handler.handleInsufficientStock(new StockInsuficienteException("Sin stock"));
        ResponseEntity<ErrorResponseDTO> r400 = handler.handleBusinessRule(new ReglaNegocioException("Regla"));
        ResponseEntity<ErrorResponseDTO> r503 = handler.handleExternalServiceUnavailable(new ServicioExternoNoDisponibleException("API caída"));
        ResponseEntity<ErrorResponseDTO> rFeign404 = handler.handleFeignException(new FeignException.NotFound("404", req, null, null));
        ResponseEntity<ErrorResponseDTO> rFeign503 = handler.handleFeignException(new FeignException.ServiceUnavailable("503", req, null, null));
        ResponseEntity<ErrorResponseDTO> rVal = handler.handleValidationExceptions(validationEx);
        ResponseEntity<ErrorResponseDTO> rMethodVal = handler.handleMethodValidationExceptions(methodValEx);
        ResponseEntity<ErrorResponseDTO> rDb = handler.handleDatabaseConstraints(new DataIntegrityViolationException("FK"));
        ResponseEntity<ErrorResponseDTO> r500 = handler.handleGenericException(new RuntimeException("Error"));

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, r404.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, rNoRes.getStatusCode());
        assertTrue(rNoRes.getBody().messages().get(0).contains("api/transactions/ruta-inexistente"));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, r405.getStatusCode());
        assertTrue(r405.getBody().messages().get(0).contains("DELETE"));
        assertEquals(HttpStatus.CONFLICT, r409.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rStock.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, r400.getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, r503.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, rFeign404.getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, rFeign503.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rVal.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rMethodVal.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, rDb.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r500.getStatusCode());
    }
}
