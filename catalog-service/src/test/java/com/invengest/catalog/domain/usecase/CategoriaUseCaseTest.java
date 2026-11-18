package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.EntidadEnUsoException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.domain.model.gateway.CategoriaGateway;
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
class CategoriaUseCaseTest {

    @Mock
    private CategoriaGateway categoriaGateway;

    private CategoriaUseCase categoriaUseCase;

    @BeforeEach
    void setUp() {
        categoriaUseCase = new CategoriaUseCase(categoriaGateway);
    }

    @Test
    @DisplayName("Gestiona CRUD de categorías y valida duplicados y productos asociados")
    void gestionaCrudYValidaDuplicadosYProductosAsociados() {
        // Arrange
        Categoria nueva = new Categoria(null, "Escolar", "Útiles");
        Categoria guardada = new Categoria(1, "Escolar", "Útiles");
        Categoria actualizada = new Categoria(1, "Oficina", "Artículos");

        when(categoriaGateway.existsByNombre("Escolar")).thenReturn(false);
        when(categoriaGateway.existsByNombre("Oficina")).thenReturn(false);
        when(categoriaGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoriaGateway.findById(1)).thenReturn(Optional.of(guardada));
        when(categoriaGateway.findAll()).thenReturn(List.of(guardada));
        when(categoriaGateway.existsProductosByCategoriaId(1)).thenReturn(true);

        // Act
        Categoria creada = categoriaUseCase.crear(nueva);
        Categoria mod = categoriaUseCase.actualizar(actualizada);
        Categoria consultada = categoriaUseCase.obtenerPorId(1);
        List<Categoria> todas = categoriaUseCase.obtenerTodas();

        // Assert
        assertEquals("Escolar", creada.getNombre());
        assertEquals("Oficina", mod.getNombre());
        assertEquals("Escolar", consultada.getNombre());
        assertEquals(1, todas.size());
        assertThrows(EntidadEnUsoException.class, () -> categoriaUseCase.eliminarPorId(1));

        when(categoriaGateway.existsProductosByCategoriaId(1)).thenReturn(false);
        categoriaUseCase.eliminarPorId(1);
        verify(categoriaGateway).deleteById(1);

        when(categoriaGateway.existsByNombre("Escolar")).thenReturn(true);
        assertThrows(EntidadDuplicadaException.class, () -> categoriaUseCase.crear(nueva));
    }

    @Test
    @DisplayName("Lanza excepciones cuando categoría no existe o el nuevo nombre está duplicado")
    void lanzaExcepcionesCuandoNoExisteODuplicadoEnActualizacion() {
        // Arrange
        Categoria existente = new Categoria(1, "Escolar", "Útiles");
        Categoria actualizada = new Categoria(1, "Oficina", "Artículos");
        Categoria inexistente = new Categoria(99, "Arte", "Pintura");

        when(categoriaGateway.findById(1)).thenReturn(Optional.of(existente));
        when(categoriaGateway.existsByNombre("Oficina")).thenReturn(true);
        when(categoriaGateway.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntidadDuplicadaException.class, () -> categoriaUseCase.actualizar(actualizada));
        assertThrows(RecursoNoEncontradoException.class, () -> categoriaUseCase.actualizar(inexistente));
        assertThrows(RecursoNoEncontradoException.class, () -> categoriaUseCase.obtenerPorId(99));
        assertThrows(RecursoNoEncontradoException.class, () -> categoriaUseCase.eliminarPorId(99));
    }
}
