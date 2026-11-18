package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.model.Categoria;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.CategoriaData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.CategoriaJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ProductoJpaRepository;
import com.invengest.catalog.infrastructure.mapper.CategoriaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaDataGatewayImplTest {

    @Mock
    private CategoriaJpaRepository categoriaJpaRepository;
    @Mock
    private ProductoJpaRepository productoJpaRepository;
    @Mock
    private CategoriaMapper categoriaMapper;

    private CategoriaDataGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new CategoriaDataGatewayImpl(categoriaJpaRepository, productoJpaRepository, categoriaMapper);
    }

    @Test
    @DisplayName("Delega operaciones de persistencia de categorías en repositorio y mapper")
    void delegaEnRepositorioYMapper() {
        // Arrange
        Categoria cat = new Categoria(1, "Escolar", "Útiles");
        CategoriaData catData = new CategoriaData(1, "Escolar", "Útiles");
        when(categoriaMapper.toEntity(cat)).thenReturn(catData);
        when(categoriaJpaRepository.save(catData)).thenReturn(catData);
        when(categoriaMapper.toDomain(catData)).thenReturn(cat);
        when(categoriaJpaRepository.findById(1)).thenReturn(Optional.of(catData));
        when(categoriaJpaRepository.findAll()).thenReturn(List.of(catData));
        when(categoriaJpaRepository.existsByNombreIgnoreCase("Escolar")).thenReturn(true);
        when(productoJpaRepository.existsByCategoria_IdCategoria(1)).thenReturn(false);

        // Act
        Categoria guardada = gateway.save(cat);
        Optional<Categoria> porId = gateway.findById(1);
        List<Categoria> todas = gateway.findAll();
        boolean existeNombre = gateway.existsByNombre("Escolar");
        boolean tieneProductos = gateway.existsProductosByCategoriaId(1);
        gateway.deleteById(1);

        // Assert
        assertEquals("Escolar", guardada.getNombre());
        assertTrue(porId.isPresent());
        assertEquals(1, todas.size());
        assertTrue(existeNombre);
        assertFalse(tieneProductos);
        verify(categoriaJpaRepository).deleteById(1);
    }
}
