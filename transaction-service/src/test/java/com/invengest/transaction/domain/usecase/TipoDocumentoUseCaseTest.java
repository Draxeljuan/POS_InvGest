package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.TipoDocumento;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoDocumentoUseCaseTest {

    @Mock
    private TipoDocumentoGateway tipoDocumentoGateway;

    @InjectMocks
    private TipoDocumentoUseCase tipoDocumentoUseCase;

    @Test
    @DisplayName("Debe listar y obtener tipo de documento por ID y validar excepciones")
    void debeListarYObtenerPorIdYValidarExcepciones() {
        // Arrange
        TipoDocumento td = new TipoDocumento(1, "CC");
        when(tipoDocumentoGateway.findById(1)).thenReturn(Optional.of(td));
        when(tipoDocumentoGateway.findById(99)).thenReturn(Optional.empty());
        when(tipoDocumentoGateway.findAll()).thenReturn(List.of(td));

        // Act
        TipoDocumento encontrado = tipoDocumentoUseCase.obtenerPorId(1);
        List<TipoDocumento> lista = tipoDocumentoUseCase.listarTodos();

        // Assert
        assertEquals("CC", encontrado.getNombre());
        assertEquals(1, lista.size());
        assertThrows(ReglaNegocioException.class, () -> tipoDocumentoUseCase.obtenerPorId(null));
        assertThrows(RecursoNoEncontradoException.class, () -> tipoDocumentoUseCase.obtenerPorId(99));
    }
}
