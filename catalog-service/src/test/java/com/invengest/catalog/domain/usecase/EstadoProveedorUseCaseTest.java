package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.EstadoProveedor;
import com.invengest.catalog.domain.model.gateway.EstadoProveedorGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadoProveedorUseCaseTest {

    @Mock
    private EstadoProveedorGateway estadoProveedorGateway;

    private EstadoProveedorUseCase estadoProveedorUseCase;

    @BeforeEach
    void setUp() {
        estadoProveedorUseCase = new EstadoProveedorUseCase(estadoProveedorGateway);
    }

    @Test
    @DisplayName("Consulta estados de proveedor por ID, nombre y listado completo")
    void consultaEstadosDeProveedor() {
        // Arrange
        EstadoProveedor activo = new EstadoProveedor(1, EstadoProveedor.ESTADO_ACTIVO);
        when(estadoProveedorGateway.findAll()).thenReturn(List.of(activo));
        when(estadoProveedorGateway.findById(1)).thenReturn(Optional.of(activo));
        when(estadoProveedorGateway.findByNombre("Activo")).thenReturn(Optional.of(activo));
        when(estadoProveedorGateway.findById(99)).thenReturn(Optional.empty());
        when(estadoProveedorGateway.findByNombre("Inexistente")).thenReturn(Optional.empty());

        // Act
        List<EstadoProveedor> todos = estadoProveedorUseCase.obtenerTodos();
        EstadoProveedor porId = estadoProveedorUseCase.obtenerPorId(1);
        EstadoProveedor porNombre = estadoProveedorUseCase.obtenerPorNombre("Activo");

        // Assert
        assertEquals(1, todos.size());
        assertEquals(1, porId.getIdEstadoProveedor());
        assertEquals("Activo", porNombre.getNombre());
        assertThrows(RecursoNoEncontradoException.class, () -> estadoProveedorUseCase.obtenerPorId(99));
        assertThrows(RecursoNoEncontradoException.class, () -> estadoProveedorUseCase.obtenerPorNombre("Inexistente"));
    }
}
