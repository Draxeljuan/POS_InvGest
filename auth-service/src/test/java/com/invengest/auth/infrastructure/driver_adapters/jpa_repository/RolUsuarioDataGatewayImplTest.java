package com.invengest.auth.infrastructure.driver_adapters.jpa_repository;

import com.invengest.auth.domain.model.RolUsuario;
import com.invengest.auth.infrastructure.driver_adapters.jpa_repository.entity.RolUsuarioData;
import com.invengest.auth.infrastructure.mapper.UsuarioEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolUsuarioDataGatewayImplTest {

    @Mock
    private RolUsuarioDataJpaRepository rolJpaRepository;

    @Mock
    private UsuarioEntityMapper entityMapper;

    @InjectMocks
    private RolUsuarioDataGatewayImpl rolGatewayImpl;

    @Test
    @DisplayName("Debe delegar consultas de roles al repositorio JPA y mapear al dominio")
    void consultasRoles_DebenDelegarYMapearCorrectamente() {
        // Arrange
        RolUsuarioData rolData = new RolUsuarioData(1, "Administrador");
        RolUsuario rolDomain = new RolUsuario(1, "Administrador");

        when(rolJpaRepository.findById(1)).thenReturn(Optional.of(rolData));
        when(rolJpaRepository.findByNombreIgnoreCase("Administrador")).thenReturn(Optional.of(rolData));
        when(rolJpaRepository.findAll()).thenReturn(List.of(rolData));
        when(entityMapper.toRolUsuario(rolData)).thenReturn(rolDomain);

        // Act
        Optional<RolUsuario> porId = rolGatewayImpl.findById(1);
        Optional<RolUsuario> porNombre = rolGatewayImpl.findByNombre("Administrador");
        List<RolUsuario> todos = rolGatewayImpl.findAll();

        // Assert
        assertTrue(porId.isPresent());
        assertTrue(porNombre.isPresent());
        assertEquals(1, todos.size());
    }
}
