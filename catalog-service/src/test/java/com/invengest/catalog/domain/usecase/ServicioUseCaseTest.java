package com.invengest.catalog.domain.usecase;

import com.invengest.catalog.domain.exception.EntidadDuplicadaException;
import com.invengest.catalog.domain.exception.RecursoNoEncontradoException;
import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.model.Servicio;
import com.invengest.catalog.domain.model.gateway.CategoriaServicioGateway;
import com.invengest.catalog.domain.model.gateway.ServicioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioUseCaseTest {

    @Mock
    private ServicioGateway servicioGateway;
    @Mock
    private CategoriaServicioGateway categoriaServicioGateway;

    private ServicioUseCase servicioUseCase;

    @BeforeEach
    void setUp() {
        servicioUseCase = new ServicioUseCase(servicioGateway, categoriaServicioGateway);
    }

    @Test
    @DisplayName("Gestiona CRUD y eliminación lógica usando categoría Deshabilitados por nombre")
    void eliminacionLogicaUsaCategoriaDeshabilitadosPorNombre() {
        // Arrange
        CategoriaServicio catImpresion = new CategoriaServicio(2, "Redacción y Documentos");
        CategoriaServicio catDeshabilitados = new CategoriaServicio(1, CategoriaServicio.CATEGORIA_DESHABILITADOS);
        Servicio servicio = new Servicio(5, catImpresion, "Anillado", "Anillado plástico", new BigDecimal("3500.00"));
        Servicio servicioMod = new Servicio(5, catImpresion, "Anillado Espiral", "Plástico", new BigDecimal("4000.00"));

        when(servicioGateway.existsByNombre("Anillado")).thenReturn(false);
        when(servicioGateway.existsByNombre("Anillado Espiral")).thenReturn(false);
        when(categoriaServicioGateway.findById(2)).thenReturn(Optional.of(catImpresion));
        when(servicioGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(servicioGateway.findById(5)).thenReturn(Optional.of(servicio));
        when(servicioGateway.findAll()).thenReturn(List.of(servicio));
        when(servicioGateway.findByCategoriaServicioId(2)).thenReturn(List.of(servicio));
        when(categoriaServicioGateway.findByNombre(CategoriaServicio.CATEGORIA_DESHABILITADOS))
                .thenReturn(Optional.of(catDeshabilitados));

        // Act
        Servicio registrado = servicioUseCase.registrar(servicio);
        Servicio actualizado = servicioUseCase.actualizar(servicioMod);
        Servicio porId = servicioUseCase.obtenerPorId(5);
        List<Servicio> todos = servicioUseCase.obtenerTodos();
        List<Servicio> porCat = servicioUseCase.obtenerPorCategoria(2);
        servicioUseCase.eliminacionLogica(5);

        // Assert
        assertEquals("Anillado", registrado.getNombre());
        assertEquals("Anillado Espiral", actualizado.getNombre());
        assertEquals(5, porId.getIdServicio());
        assertEquals(1, todos.size());
        assertEquals(1, porCat.size());
        assertEquals(CategoriaServicio.CATEGORIA_DESHABILITADOS, servicio.getCategoriaServicio().getNombre());

        when(servicioGateway.existsByNombre("Anillado")).thenReturn(true);
        assertThrows(EntidadDuplicadaException.class, () -> servicioUseCase.registrar(servicio));
    }

    @Test
    @DisplayName("Lanza excepciones cuando servicio o categoría de servicio no existe")
    void lanzaExcepcionesCuandoServicioOCategoriaNoExiste() {
        // Arrange
        Servicio sinCategoria = new Servicio(1, null, "Fotocopia", "B/N", BigDecimal.TEN);
        when(servicioGateway.existsByNombre("Fotocopia")).thenReturn(false);
        when(servicioGateway.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> servicioUseCase.registrar(sinCategoria));
        assertThrows(RecursoNoEncontradoException.class, () -> servicioUseCase.obtenerPorId(99));
        assertThrows(RecursoNoEncontradoException.class, () -> servicioUseCase.eliminacionLogica(99));
    }
}
