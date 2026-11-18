package com.invengest.analytics.infrastructure.entry_points.rest;

import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.domain.usecase.ConsultarTopProductosUseCase;
import com.invengest.analytics.infrastructure.entry_points.dto.response.TopProductoResponseDTO;
import com.invengest.analytics.infrastructure.mapper.TopProductoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopProductoControllerTest {

    @Mock
    private ConsultarTopProductosUseCase consultarTopProductosUseCase;

    @Mock
    private TopProductoMapper topProductoMapper;

    @InjectMocks
    private TopProductoController controller;

    @Test
    @DisplayName("Debe obtener top productos por fecha y por rango")
    void obtenerPorFechaYRango_Exitoso() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        List<TopProducto> lista = List.of(TopProducto.crearInicial("P1", "Prod", fecha));
        List<TopProductoResponseDTO> dtos = List.of(TopProductoResponseDTO.builder().idProducto("P1").build());

        when(consultarTopProductosUseCase.obtenerPorFecha(fecha)).thenReturn(lista);
        when(consultarTopProductosUseCase.obtenerTopPorRango(fecha, fecha, 10)).thenReturn(lista);
        when(topProductoMapper.toResponseList(lista)).thenReturn(dtos);

        // Act
        ResponseEntity<List<TopProductoResponseDTO>> respFecha = controller.obtenerPorFecha(fecha);
        ResponseEntity<List<TopProductoResponseDTO>> respRango = controller.obtenerPorRango(fecha, fecha, 10);

        // Assert
        assertThat(respFecha.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respFecha.getBody()).hasSize(1);
        assertThat(respRango.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respRango.getBody()).hasSize(1);
    }
}
