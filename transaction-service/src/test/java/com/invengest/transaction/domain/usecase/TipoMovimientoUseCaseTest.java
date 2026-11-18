package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.TipoMovimiento;
import com.invengest.transaction.domain.model.gateway.TipoMovimientoGateway;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoMovimientoUseCaseTest {

    @Mock
    private TipoMovimientoGateway tipoMovimientoGateway;

    @InjectMocks
    private TipoMovimientoUseCase tipoMovimientoUseCase;

    @Test
    @DisplayName("Debe listar y obtener tipo de movimiento por ID y validar excepciones")
    void debeListarYObtenerPorIdYValidarExcepciones() {
        // Arrange
        TipoMovimiento tm = new TipoMovimiento(1, "Ingreso por Compra");
        when(tipoMovimientoGateway.findById(1)).thenReturn(Optional.of(tm));
        when(tipoMovimientoGateway.findById(99)).thenReturn(Optional.empty());
        when(tipoMovimientoGateway.findAll()).thenReturn(List.of(tm));

        // Act
        TipoMovimiento encontrado = tipoMovimientoUseCase.obtenerPorId(1);
        List<TipoMovimiento> lista = tipoMovimientoUseCase.listarTodos();

        // Assert
        assertEquals("Ingreso por Compra", encontrado.getNombre());
        assertEquals(1, lista.size());
        assertThrows(ReglaNegocioException.class, () -> tipoMovimientoUseCase.obtenerPorId(null));
        assertThrows(RecursoNoEncontradoException.class, () -> tipoMovimientoUseCase.obtenerPorId(99));
    }
}
