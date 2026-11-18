package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.EntidadEnUsoException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.model.gateway.CategoriaServicioGateway;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServicioUseCaseTest {

    @Mock
    private CategoriaServicioGateway categoriaServicioGateway;

    private CategoriaServicioUseCase categoriaServicioUseCase;

    @BeforeEach
    void setUp() {
        categoriaServicioUseCase = new CategoriaServicioUseCase(categoriaServicioGateway);
    }

    @Test
    @DisplayName("Gestiona CRUD de categorías de servicio y valida servicios asociados")
    void gestionaCrudYValidaServiciosAsociados() {
        // Arrange
        CategoriaServicio cs = new CategoriaServicio(1, "Trámites Digitales");
        when(categoriaServicioGateway.save(any())).thenReturn(cs);
        when(categoriaServicioGateway.findById(1)).thenReturn(Optional.of(cs));
        when(categoriaServicioGateway.findAll()).thenReturn(List.of(cs));
        when(categoriaServicioGateway.existsServiciosByCategoriaServicioId(1)).thenReturn(false);

        // Act
        CategoriaServicio creada = categoriaServicioUseCase.crear(cs);
        CategoriaServicio actualizada = categoriaServicioUseCase.actualizar(cs);
        CategoriaServicio porId = categoriaServicioUseCase.obtenerPorId(1);
        List<CategoriaServicio> todas = categoriaServicioUseCase.obtenerTodas();
        categoriaServicioUseCase.eliminarPorId(1);

        // Assert
        assertEquals("Trámites Digitales", creada.getNombre());
        assertEquals("Trámites Digitales", actualizada.getNombre());
        assertEquals(1, porId.getIdCategoriaServicio());
        assertEquals(1, todas.size());
        verify(categoriaServicioGateway).deleteById(1);
    }

    @Test
    @DisplayName("Lanza excepciones cuando categoría de servicio no existe o está en uso")
    void lanzaExcepcionesCuandoNoExisteOEstaEnUso() {
        // Arrange
        CategoriaServicio cs = new CategoriaServicio(1, "Impresión");
        CategoriaServicio sinId = new CategoriaServicio(null, "Impresión");
        when(categoriaServicioGateway.findById(1)).thenReturn(Optional.of(cs));
        when(categoriaServicioGateway.existsServiciosByCategoriaServicioId(1)).thenReturn(true);
        when(categoriaServicioGateway.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntidadEnUsoException.class, () -> categoriaServicioUseCase.eliminarPorId(1));
        assertThrows(RecursoNoEncontradoException.class, () -> categoriaServicioUseCase.actualizar(sinId));
        assertThrows(RecursoNoEncontradoException.class, () -> categoriaServicioUseCase.obtenerPorId(99));
        assertThrows(RecursoNoEncontradoException.class, () -> categoriaServicioUseCase.eliminarPorId(99));
    }
}
