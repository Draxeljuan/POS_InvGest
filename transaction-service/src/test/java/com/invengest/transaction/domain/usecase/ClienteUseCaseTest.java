package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.EntidadDuplicadaException;
import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.domain.model.gateway.ClienteGateway;
import com.invengest.transaction.domain.model.gateway.TipoDocumentoGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteUseCaseTest {

    @Mock
    private ClienteGateway clienteGateway;
    @Mock
    private TipoDocumentoGateway tipoDocumentoGateway;

    @InjectMocks
    private ClienteUseCase clienteUseCase;

    @Test
    @DisplayName("Debe registrar y consultar clientes por ID, documento y listado completo")
    void debeRegistrarYConsultarClientes() {
        // Arrange
        Cliente nuevoCliente = new Cliente(1, new TipoDocumento(1, null), "9001", "Pedro", null, "Ruiz", null, "300", "p@test.com");
        when(clienteGateway.findByDocumentoIdentidad("9001")).thenReturn(Optional.empty(), Optional.of(nuevoCliente));
        when(tipoDocumentoGateway.findById(1)).thenReturn(Optional.of(new TipoDocumento(1, "CC")));
        when(clienteGateway.save(any())).thenReturn(nuevoCliente);
        when(clienteGateway.findById(1)).thenReturn(Optional.of(nuevoCliente));
        when(clienteGateway.findAll()).thenReturn(List.of(nuevoCliente));

        // Act
        Cliente registrado = clienteUseCase.registrar(nuevoCliente);
        Cliente porId = clienteUseCase.obtenerPorId(1);
        Cliente porDoc = clienteUseCase.obtenerPorDocumento("9001");
        List<Cliente> todos = clienteUseCase.listarTodos();

        // Assert
        assertEquals("Pedro Ruiz", registrado.getNombreCompleto());
        assertEquals("Pedro Ruiz", porId.getNombreCompleto());
        assertEquals("Pedro Ruiz", porDoc.getNombreCompleto());
        assertEquals(1, todos.size());
    }

    @Test
    @DisplayName("Debe validar duplicados, campos obligatorios y existencia en ClienteUseCase")
    void debeValidarDuplicadosYCamposObligatorios() {
        // Arrange
        Cliente existente = new Cliente(1, new TipoDocumento(99, null), "9001", "Pedro", null, "Ruiz", null, "300", "p@test.com");
        when(clienteGateway.findByDocumentoIdentidad("9001")).thenReturn(Optional.of(existente));
        when(clienteGateway.findByDocumentoIdentidad("000")).thenReturn(Optional.empty());
        when(clienteGateway.findById(99)).thenReturn(Optional.empty());
        when(tipoDocumentoGateway.findById(99)).thenReturn(Optional.empty());

        Cliente conTipoInexistente = new Cliente(null, new TipoDocumento(99, null), null, "Pedro", null, "Ruiz", null, null, null);

        // Act & Assert
        assertThrows(EntidadDuplicadaException.class, () -> clienteUseCase.registrar(existente));
        assertThrows(RecursoNoEncontradoException.class, () -> clienteUseCase.registrar(conTipoInexistente));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.registrar(null));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.registrar(new Cliente(null, null, null, "", null, "Ruiz", null, null, null)));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.registrar(new Cliente(null, null, null, null, null, "Ruiz", null, null, null)));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.registrar(new Cliente(null, null, null, "Pedro", null, "", null, null, null)));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.registrar(new Cliente(null, null, null, "Pedro", null, null, null, null, null)));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.obtenerPorId(null));
        assertThrows(RecursoNoEncontradoException.class, () -> clienteUseCase.obtenerPorId(99));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.obtenerPorDocumento(" "));
        assertThrows(ReglaNegocioException.class, () -> clienteUseCase.obtenerPorDocumento(null));
        assertThrows(RecursoNoEncontradoException.class, () -> clienteUseCase.obtenerPorDocumento("000"));
    }
}
