package com.invengest.transaction.infrastructure.entry_points.rest;

import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.usecase.TipoMovimientoUseCase;
import com.invengest.transaction.infrastructure.entry_points.dto.response.TipoMovimientoResponse;
import com.invengest.transaction.infrastructure.mapper.TipoMovimientoRestMapper;
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
class TipoMovimientoControllerTest {

    @Mock
    private TipoMovimientoUseCase tipoMovimientoUseCase;
    @Mock
    private TipoMovimientoRestMapper mapper;

    @InjectMocks
    private TipoMovimientoController controller;

    @Test
    @DisplayName("Debe listar todos los tipos de movimiento y consultar por ID")
    void debeListarYObtenerPorId() {
        // Arrange
        TipoMovimiento tm = new TipoMovimiento(1, "Ingreso por Compra");
        TipoMovimientoResponse resp = new TipoMovimientoResponse(1, "Ingreso por Compra");
        when(tipoMovimientoUseCase.listarTodos()).thenReturn(List.of(tm));
        when(tipoMovimientoUseCase.obtenerPorId(1)).thenReturn(tm);
        when(mapper.toResponse(tm)).thenReturn(resp);

        // Act
        ResponseEntity<List<TipoMovimientoResponse>> rLista = controller.listarTodos();
        ResponseEntity<TipoMovimientoResponse> rId = controller.obtenerPorId(1);

        // Assert
        assertEquals(HttpStatus.OK, rLista.getStatusCode());
        assertEquals(1, rLista.getBody().size());
        assertEquals(HttpStatus.OK, rId.getStatusCode());
        assertEquals("Ingreso por Compra", rId.getBody().nombre());
    }
}
