package com.invengest.analytics.infrastructure.entry_points.rest;

import com.invengest.analytics.domain.model.ReporteEstacionalidad;
import com.invengest.analytics.domain.usecase.ConsultarEstacionalidadUseCase;
import com.invengest.analytics.infrastructure.entry_points.dto.response.EstacionalidadResponseDTO;
import com.invengest.analytics.infrastructure.mapper.AnaliticaRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstacionalidadControllerTest {

    @Mock
    private ConsultarEstacionalidadUseCase consultarEstacionalidadUseCase;

    @Mock
    private AnaliticaRestMapper analiticaRestMapper;

    @InjectMocks
    private EstacionalidadController controller;

    @Test
    @DisplayName("Debe obtener reporte de estacionalidad mensual y mapear respuesta")
    void obtenerEstacionalidadMensual_Exitoso() {
        // Arrange
        ReporteEstacionalidad domain = ReporteEstacionalidad.builder().anio(2026).mes(11).diasHabiles(24).build();
        EstacionalidadResponseDTO dto = EstacionalidadResponseDTO.builder().anio(2026).mes(11).diasHabiles(24).build();

        when(consultarEstacionalidadUseCase.analizarEstacionalidadMensual(2026, 11)).thenReturn(domain);
        when(analiticaRestMapper.toEstacionalidadResponse(domain)).thenReturn(dto);

        // Act
        ResponseEntity<EstacionalidadResponseDTO> response = controller.obtenerEstacionalidadMensual(2026, 11);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDiasHabiles()).isEqualTo(24);
    }
}
