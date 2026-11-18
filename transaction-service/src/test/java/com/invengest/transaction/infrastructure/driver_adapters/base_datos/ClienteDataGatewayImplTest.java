package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.ClienteData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.ClienteJpaRepository;
import com.invengest.transaction.infrastructure.mapper.ClienteMapper;
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
class ClienteDataGatewayImplTest {

    @Mock
    private ClienteJpaRepository repository;
    @Mock
    private ClienteMapper mapper;

    @InjectMocks
    private ClienteDataGatewayImpl gateway;

    @Test
    @DisplayName("Debe persistir y consultar clientes por ID y documento")
    void debePersistirYConsultarClientes() {
        // Arrange
        Cliente cliDom = new Cliente();
        ClienteData cliEnt = new ClienteData();
        when(mapper.toEntity(cliDom)).thenReturn(cliEnt);
        when(repository.save(cliEnt)).thenReturn(cliEnt);
        when(mapper.toDomain(cliEnt)).thenReturn(cliDom);
        when(repository.findById(1)).thenReturn(Optional.of(cliEnt));
        when(repository.findByDocumentoIdentidad("123")).thenReturn(Optional.of(cliEnt));
        when(repository.findAll()).thenReturn(List.of(cliEnt));
        when(repository.existsById(1)).thenReturn(true);

        // Act
        Cliente guardado = gateway.save(cliDom);
        Optional<Cliente> porId = gateway.findById(1);
        Optional<Cliente> porDoc = gateway.findByDocumentoIdentidad("123");
        List<Cliente> todos = gateway.findAll();
        boolean existe = gateway.existsById(1);

        // Assert
        assertEquals(cliDom, guardado);
        assertTrue(porId.isPresent());
        assertTrue(porDoc.isPresent());
        assertEquals(1, todos.size());
        assertTrue(existe);
    }
}
