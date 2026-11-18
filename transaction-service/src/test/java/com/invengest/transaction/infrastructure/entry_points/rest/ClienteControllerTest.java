package com.invengest.transaction.infrastructure.entry_points.rest;

import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.usecase.ClienteUseCase;
import com.invengest.transaction.infrastructure.entry_points.dto.request.ClienteRequest;
import com.invengest.transaction.infrastructure.entry_points.dto.response.ClienteResponse;
import com.invengest.transaction.infrastructure.entry_points.dto.response.TipoDocumentoResponse;
import com.invengest.transaction.infrastructure.mapper.ClienteRestMapper;
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
class ClienteControllerTest {

    @Mock
    private ClienteUseCase clienteUseCase;
    @Mock
    private ClienteRestMapper mapper;

    @InjectMocks
    private ClienteController controller;

    @Test
    @DisplayName("Debe registrar y consultar clientes por ID, documento y listado completo")
    void debeRegistrarYConsultarClientes() {
        // Arrange
        Cliente cli = new Cliente();
        ClienteResponse cliResp = new ClienteResponse(1, new TipoDocumentoResponse(1, "CC"),
                "100", "Juan", null, "Pérez", null, "Juan Pérez", "300", "j@test.com");
        ClienteRequest req = new ClienteRequest(1, "100", "Juan", null, "Pérez", null, "300", "j@test.com");

        when(mapper.toDomain(req)).thenReturn(cli);
        when(clienteUseCase.registrar(cli)).thenReturn(cli);
        when(clienteUseCase.obtenerPorId(1)).thenReturn(cli);
        when(clienteUseCase.obtenerPorDocumento("100")).thenReturn(cli);
        when(clienteUseCase.listarTodos()).thenReturn(List.of(cli));
        when(mapper.toResponse(cli)).thenReturn(cliResp);

        // Act
        ResponseEntity<ClienteResponse> rReg = controller.registrar(req);
        ResponseEntity<ClienteResponse> rId = controller.obtenerPorId(1);
        ResponseEntity<ClienteResponse> rDoc = controller.obtenerPorDocumento("100");
        ResponseEntity<List<ClienteResponse>> rLista = controller.listarTodos();

        // Assert
        assertEquals(HttpStatus.CREATED, rReg.getStatusCode());
        assertEquals(HttpStatus.OK, rId.getStatusCode());
        assertEquals(HttpStatus.OK, rDoc.getStatusCode());
        assertEquals(1, rLista.getBody().size());
    }
}
