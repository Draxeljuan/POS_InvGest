package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.gateway.MetodoPagoGateway;
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
class MetodoPagoUseCaseTest {

    @Mock
    private MetodoPagoGateway metodoPagoGateway;

    @InjectMocks
    private MetodoPagoUseCase metodoPagoUseCase;

    @Test
    @DisplayName("Debe listar y obtener método de pago por ID y validar excepciones")
    void debeListarYObtenerPorIdYValidarExcepciones() {
        // Arrange
        MetodoPago mp = new MetodoPago(1, "Efectivo");
        when(metodoPagoGateway.findById(1)).thenReturn(Optional.of(mp));
        when(metodoPagoGateway.findById(99)).thenReturn(Optional.empty());
        when(metodoPagoGateway.findAll()).thenReturn(List.of(mp));

        // Act
        MetodoPago encontrado = metodoPagoUseCase.obtenerPorId(1);
        List<MetodoPago> lista = metodoPagoUseCase.listarTodos();

        // Assert
        assertEquals("Efectivo", encontrado.getNombre());
        assertEquals(1, lista.size());
        assertThrows(ReglaNegocioException.class, () -> metodoPagoUseCase.obtenerPorId(null));
        assertThrows(RecursoNoEncontradoException.class, () -> metodoPagoUseCase.obtenerPorId(99));
    }
}
