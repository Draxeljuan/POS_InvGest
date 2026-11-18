package com.invengest.catalog.infrastructure.driver_adapters.base_datos;

import com.invengest.catalog.domain.model.CategoriaServicio;
import com.invengest.catalog.domain.model.Servicio;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.CategoriaServicioData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.entity.ServicioData;
import com.invengest.catalog.infrastructure.driver_adapters.base_datos.repository.ServicioJpaRepository;
import com.invengest.catalog.infrastructure.mapper.ServicioMapper;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioDataGatewayImplTest {

    @Mock
    private ServicioJpaRepository servicioJpaRepository;
    @Mock
    private ServicioMapper servicioMapper;

    private ServicioDataGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new ServicioDataGatewayImpl(servicioJpaRepository, servicioMapper);
    }

    @Test
    @DisplayName("Delega operaciones de persistencia de servicios en repositorio y mapper")
    void delegaEnRepositorioYMapper() {
        // Arrange
        CategoriaServicio cs = new CategoriaServicio(1, "Impresión");
        CategoriaServicioData csData = new CategoriaServicioData(1, "Impresión");
        Servicio serv = new Servicio(1, cs, "Fotocopia", "B/N", BigDecimal.TEN);
        ServicioData servData = new ServicioData(1, csData, "Fotocopia", "B/N", BigDecimal.TEN);
        when(servicioMapper.toEntity(serv)).thenReturn(servData);
        when(servicioJpaRepository.save(servData)).thenReturn(servData);
        when(servicioJpaRepository.findById(1)).thenReturn(Optional.of(servData));
        when(servicioJpaRepository.findAll()).thenReturn(List.of(servData));
        when(servicioJpaRepository.existsByNombreIgnoreCase("Fotocopia")).thenReturn(true);
        when(servicioJpaRepository.findByCategoriaServicio_IdCategoriaServicio(1)).thenReturn(List.of(servData));
        when(servicioMapper.toDomain(servData)).thenReturn(serv);

        // Act
        Servicio guardado = gateway.save(serv);
        Optional<Servicio> porId = gateway.findById(1);
        List<Servicio> todos = gateway.findAll();
        boolean existeNombre = gateway.existsByNombre("Fotocopia");
        List<Servicio> porCategoria = gateway.findByCategoriaServicioId(1);

        // Assert
        assertEquals("Fotocopia", guardado.getNombre());
        assertTrue(porId.isPresent());
        assertEquals(1, todos.size());
        assertTrue(existeNombre);
        assertEquals(1, porCategoria.size());
    }
}
