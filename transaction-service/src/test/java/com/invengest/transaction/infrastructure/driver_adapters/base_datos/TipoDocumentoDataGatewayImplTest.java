package com.invengest.transaction.infrastructure.driver_adapters.base_datos;

import com.invengest.transaction.domain.model.TipoDocumento;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.entity.TipoDocumentoData;
import com.invengest.transaction.infrastructure.driver_adapters.base_datos.repository.TipoDocumentoJpaRepository;
import com.invengest.transaction.infrastructure.mapper.TipoDocumentoMapper;
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
class TipoDocumentoDataGatewayImplTest {

    @Mock
    private TipoDocumentoJpaRepository repository;
    @Mock
    private TipoDocumentoMapper mapper;

    @InjectMocks
    private TipoDocumentoDataGatewayImpl gateway;

    @Test
    @DisplayName("Debe consultar tipos de documento por ID y listado completo")
    void debeConsultarTiposDocumento() {
        // Arrange
        TipoDocumento tdDom = new TipoDocumento(1, "CC");
        TipoDocumentoData tdEnt = new TipoDocumentoData(1, "CC");
        when(repository.findById(1)).thenReturn(Optional.of(tdEnt));
        when(repository.findAll()).thenReturn(List.of(tdEnt));
        when(mapper.toDomain(tdEnt)).thenReturn(tdDom);

        // Act
        Optional<TipoDocumento> porId = gateway.findById(1);
        List<TipoDocumento> todos = gateway.findAll();

        // Assert
        assertTrue(porId.isPresent());
        assertEquals(1, todos.size());
    }
}
