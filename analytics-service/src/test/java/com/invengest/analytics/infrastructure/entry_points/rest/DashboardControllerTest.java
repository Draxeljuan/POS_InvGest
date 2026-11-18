package com.invengest.analytics.infrastructure.entry_points.rest;

import com.invengest.analytics.domain.model.DashboardResumen;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.usecase.ConsultarDashboardUseCase;
import com.invengest.analytics.infrastructure.entry_points.dto.response.DashboardResponseDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.response.ResumenVentaResponseDTO;
import com.invengest.analytics.infrastructure.mapper.DashboardRestMapper;
import com.invengest.analytics.infrastructure.mapper.ResumenVentaMapper;
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
class DashboardControllerTest {

    @Mock
    private ConsultarDashboardUseCase consultarDashboardUseCase;

    @Mock
    private DashboardRestMapper dashboardRestMapper;

    @Mock
    private ResumenVentaMapper resumenVentaMapper;

    @InjectMocks
    private DashboardController controller;

    @Test
    @DisplayName("Debe obtener resumen del dashboard y consultar ventas por rango")
    void obtenerResumenYVentasPorRango_Exitoso() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        DashboardResumen domain = DashboardResumen.builder().fechaConsulta(fecha).build();
        DashboardResponseDTO dto = DashboardResponseDTO.builder().fechaConsulta(fecha).build();
        List<ResumenVentasDiarias> ventas = List.of(ResumenVentasDiarias.crearVacio(fecha));
        List<ResumenVentaResponseDTO> ventasDto = List.of(ResumenVentaResponseDTO.builder().fecha(fecha).build());

        when(consultarDashboardUseCase.obtenerResumenDashboard(fecha, 7)).thenReturn(domain);
        when(dashboardRestMapper.toResponse(domain)).thenReturn(dto);
        when(consultarDashboardUseCase.consultarVentasPorRango(fecha, fecha)).thenReturn(ventas);
        when(resumenVentaMapper.toResponseList(ventas)).thenReturn(ventasDto);

        // Act
        ResponseEntity<DashboardResponseDTO> respDashboard = controller.obtenerResumenDashboard(fecha, 7);
        ResponseEntity<List<ResumenVentaResponseDTO>> respVentas = controller.consultarVentasPorRango(fecha, fecha);

        // Assert
        assertThat(respDashboard.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respDashboard.getBody()).isNotNull();
        assertThat(respVentas.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respVentas.getBody()).hasSize(1);
    }
}
