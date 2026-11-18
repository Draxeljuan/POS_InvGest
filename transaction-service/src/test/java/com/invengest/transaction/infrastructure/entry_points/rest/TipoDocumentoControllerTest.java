package com.invengest.transaction.infrastructure.entry_points.rest;

import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.domain.usecase.TipoDocumentoUseCase;
import com.invengest.transaction.infrastructure.entry_points.dto.response.TipoDocumentoResponse;
import com.invengest.transaction.infrastructure.mapper.TipoDocumentoRestMapper;
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
class TipoDocumentoControllerTest {

    @Mock
    private TipoDocumentoUseCase tipoDocumentoUseCase;
    @Mock
    private TipoDocumentoRestMapper mapper;

    @InjectMocks
    private TipoDocumentoController controller;

    @Test
    @DisplayName("Debe listar todos los tipos de documento y consultar por ID")
    void debeListarYObtenerPorId() {
        // Arrange
        TipoDocumento td = new TipoDocumento(1, "CC");
        TipoDocumentoResponse resp = new TipoDocumentoResponse(1, "CC");
        when(tipoDocumentoUseCase.listarTodos()).thenReturn(List.of(td));
        when(tipoDocumentoUseCase.obtenerPorId(1)).thenReturn(td);
        when(mapper.toResponse(td)).thenReturn(resp);

        // Act
        ResponseEntity<List<TipoDocumentoResponse>> rLista = controller.listarTodos();
        ResponseEntity<TipoDocumentoResponse> rId = controller.obtenerPorId(1);

        // Assert
        assertEquals(HttpStatus.OK, rLista.getStatusCode());
        assertEquals(1, rLista.getBody().size());
        assertEquals(HttpStatus.OK, rId.getStatusCode());
        assertEquals("CC", rId.getBody().nombre());
    }
}
