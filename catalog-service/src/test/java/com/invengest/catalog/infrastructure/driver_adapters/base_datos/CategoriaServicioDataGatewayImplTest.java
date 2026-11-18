package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.CategoriaServicioData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.CategoriaServicioJpaRepository;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ServicioJpaRepository;
import com.invengest.catalog.infrastructure.mapper.CategoriaServicioMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServicioDataGatewayImplTest {

    @Mock
    private CategoriaServicioJpaRepository categoriaServicioJpaRepository;
    @Mock
    private ServicioJpaRepository servicioJpaRepository;
    @Mock
    private CategoriaServicioMapper categoriaServicioMapper;

    private CategoriaServicioDataGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new CategoriaServicioDataGatewayImpl(
                categoriaServicioJpaRepository, servicioJpaRepository, categoriaServicioMapper
        );
    }

    @Test
    @DisplayName("Delega operaciones de persistencia de categorías de servicio en repositorio y mapper")
    void delegaEnRepositorioYMapper() {
        // Arrange
        CategoriaServicio cs = new CategoriaServicio(1, "Deshabilitados");
        CategoriaServicioData csData = new CategoriaServicioData(1, "Deshabilitados");
        when(categoriaServicioMapper.toEntity(cs)).thenReturn(csData);
        when(categoriaServicioJpaRepository.save(csData)).thenReturn(csData);
        when(categoriaServicioJpaRepository.findById(1)).thenReturn(Optional.of(csData));
        when(categoriaServicioJpaRepository.findAll()).thenReturn(List.of(csData));
        when(categoriaServicioJpaRepository.findByNombreIgnoreCase("Deshabilitados")).thenReturn(Optional.of(csData));
        when(categoriaServicioMapper.toDomain(csData)).thenReturn(cs);
        when(servicioJpaRepository.existsByCategoriaServicio_IdCategoriaServicio(1)).thenReturn(true);

        // Act
        CategoriaServicio guardada = gateway.save(cs);
        Optional<CategoriaServicio> porId = gateway.findById(1);
        List<CategoriaServicio> todas = gateway.findAll();
        Optional<CategoriaServicio> porNombre = gateway.findByNombre("Deshabilitados");
        boolean tieneServicios = gateway.existsServiciosByCategoriaServicioId(1);
        gateway.deleteById(1);

        // Assert
        assertEquals("Deshabilitados", guardada.getNombre());
        assertTrue(porId.isPresent());
        assertEquals(1, todas.size());
        assertTrue(porNombre.isPresent());
        assertTrue(tieneServicios);
        verify(categoriaServicioJpaRepository).deleteById(1);
    }
}
