package com.invengest.transaction.infrastructure.entry_points.rest;

import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.usecase.MetodoPagoUseCase;
import com.invengest.transaction.infrastructure.entry_points.dto.response.MetodoPagoResponse;
import com.invengest.transaction.infrastructure.mapper.MetodoPagoRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetodoPagoControllerTest {

    @Mock
    private MetodoPagoUseCase metodoPagoUseCase;
    @Mock
    private MetodoPagoRestMapper mapper;

    @InjectMocks
    private MetodoPagoController controller;

    @Test
    @DisplayName("Debe listar todos los métodos de pago y consultar por ID")
    void debeListarYObtenerPorId() {
        // Arrange
        MetodoPago mp = new MetodoPago(1, "Efectivo");
        MetodoPagoResponse resp = new MetodoPagoResponse(1, "Efectivo");
        when(metodoPagoUseCase.listarTodos()).thenReturn(List.of(mp));
        when(metodoPagoUseCase.obtenerPorId(1)).thenReturn(mp);
        when(mapper.toResponse(mp)).thenReturn(resp);

        // Act
        ResponseEntity<List<MetodoPagoResponse>> rLista = controller.listarTodos();
        ResponseEntity<MetodoPagoResponse> rId = controller.obtenerPorId(1);

        // Assert
        assertEquals(HttpStatus.OK, rLista.getStatusCode());
        assertEquals(1, rLista.getBody().size());
        assertEquals(HttpStatus.OK, rId.getStatusCode());
        assertEquals("Efectivo", rId.getBody().nombre());
    }
}
