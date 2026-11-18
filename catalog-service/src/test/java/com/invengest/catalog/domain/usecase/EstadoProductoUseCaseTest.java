package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.EstadoProducto;
import com.invengest.catalog.domain.model.gateway.EstadoProductoGateway;
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
class EstadoProductoUseCaseTest {

    @Mock
    private EstadoProductoGateway estadoProductoGateway;

    private EstadoProductoUseCase estadoProductoUseCase;

    @BeforeEach
    void setUp() {
        estadoProductoUseCase = new EstadoProductoUseCase(estadoProductoGateway);
    }

    @Test
    @DisplayName("Consulta estados de producto por ID, nombre y listado completo")
    void consultaEstadosDeProducto() {
        // Arrange
        EstadoProducto activo = new EstadoProducto(1, EstadoProducto.ESTADO_ACTIVO);
        when(estadoProductoGateway.findAll()).thenReturn(List.of(activo));
        when(estadoProductoGateway.findById(1)).thenReturn(Optional.of(activo));
        when(estadoProductoGateway.findByNombre("Activo")).thenReturn(Optional.of(activo));
        when(estadoProductoGateway.findById(99)).thenReturn(Optional.empty());
        when(estadoProductoGateway.findByNombre("Inexistente")).thenReturn(Optional.empty());

        // Act
        List<EstadoProducto> todos = estadoProductoUseCase.obtenerTodos();
        EstadoProducto porId = estadoProductoUseCase.obtenerPorId(1);
        EstadoProducto porNombre = estadoProductoUseCase.obtenerPorNombre("Activo");

        // Assert
        assertEquals(1, todos.size());
        assertEquals(1, porId.getIdEstado());
        assertEquals("Activo", porNombre.getNombre());
        assertThrows(RecursoNoEncontradoException.class, () -> estadoProductoUseCase.obtenerPorId(99));
        assertThrows(RecursoNoEncontradoException.class, () -> estadoProductoUseCase.obtenerPorNombre("Inexistente"));
    }
}
