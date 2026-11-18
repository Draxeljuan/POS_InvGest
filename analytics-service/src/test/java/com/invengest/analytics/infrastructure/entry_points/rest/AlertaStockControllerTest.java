package com.invengest.analytics.infrastructure.entry_points.rest;

import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.usecase.ConsultarAlertasStockUseCase;
import com.invengest.analytics.infrastructure.entry_points.dto.response.AlertaStockResponseDTO;
import com.invengest.analytics.infrastructure.mapper.AlertaStockMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaStockControllerTest {

    @Mock
    private ConsultarAlertasStockUseCase consultarAlertasStockUseCase;

    @Mock
    private AlertaStockMapper alertaStockMapper;

    @InjectMocks
    private AlertaStockController controller;

    @Test
    @DisplayName("Debe obtener alertas totales, filtradas por estado y recientes")
    void obtenerAlertasYRecientes_Exitoso() {
        // Arrange
        List<AlertaStock> lista = List.of(AlertaStock.builder().idProducto("P1").build());
        List<AlertaStockResponseDTO> dtos = List.of(AlertaStockResponseDTO.builder().idProducto("P1").build());

        when(consultarAlertasStockUseCase.obtenerTodas()).thenReturn(lista);
        when(consultarAlertasStockUseCase.obtenerPorEstado("Bajo Stock")).thenReturn(lista);
        when(consultarAlertasStockUseCase.obtenerRecientes(5)).thenReturn(lista);
        when(alertaStockMapper.toResponseList(lista)).thenReturn(dtos);

        // Act
        ResponseEntity<List<AlertaStockResponseDTO>> respTodas = controller.obtenerAlertas(null);
        ResponseEntity<List<AlertaStockResponseDTO>> respBlanco = controller.obtenerAlertas(" ");
        ResponseEntity<List<AlertaStockResponseDTO>> respEstado = controller.obtenerAlertas("Bajo Stock");
        ResponseEntity<List<AlertaStockResponseDTO>> respRecientes = controller.obtenerAlertasRecientes(5);

        // Assert
        assertThat(respTodas.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respBlanco.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respEstado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respRecientes.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
