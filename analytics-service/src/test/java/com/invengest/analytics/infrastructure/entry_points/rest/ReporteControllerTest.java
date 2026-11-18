package com.invengest.analytics.infrastructure.entry_points.rest;

import com.invengest.analytics.domain.model.ResumenVentasUsd;
import com.invengest.analytics.domain.usecase.GenerarReporteControlVentasUseCase;
import com.invengest.analytics.infrastructure.entry_points.dto.response.ResumenVentasUsdResponseDTO;
import com.invengest.analytics.infrastructure.mapper.AnaliticaRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    @Mock
    private GenerarReporteControlVentasUseCase generarReporteUseCase;

    @Mock
    private AnaliticaRestMapper analiticaRestMapper;

    @InjectMocks
    private ReporteController controller;

    @Test
    @DisplayName("Debe descargar reportes Excel/PDF y consultar resumen de ventas en USD")
    void descargarReportesYResumenUsd_Exitoso() {
        // Arrange
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);
        byte[] dummyExcel = new byte[]{80, 75, 3, 4};
        byte[] dummyPdf = new byte[]{37, 80, 68, 70};
        ResumenVentasUsd domainUsd = ResumenVentasUsd.builder().totalNetoUsd(new BigDecimal("100.00")).build();
        ResumenVentasUsdResponseDTO dtoUsd = ResumenVentasUsdResponseDTO.builder().totalNetoUsd(new BigDecimal("100.00")).build();

        when(generarReporteUseCase.generarExcelControlVentas(2026, 11)).thenReturn(dummyExcel);
        when(generarReporteUseCase.generarPdfVentasPorRango(inicio, fin)).thenReturn(dummyPdf);
        when(generarReporteUseCase.generarPdfAlertasStock()).thenReturn(dummyPdf);
        when(generarReporteUseCase.obtenerResumenVentasUsd(inicio, fin)).thenReturn(domainUsd);
        when(analiticaRestMapper.toUsdResponse(domainUsd)).thenReturn(dtoUsd);

        // Act
        ResponseEntity<byte[]> respExcel = controller.descargarReporteControlVentasExcel(2026, 11);
        ResponseEntity<byte[]> respPdfVentas = controller.descargarReporteVentasPdf(inicio, fin);
        ResponseEntity<byte[]> respPdfAlertas = controller.descargarReporteAlertasStockPdf();
        ResponseEntity<ResumenVentasUsdResponseDTO> respUsd = controller.obtenerResumenVentasUsd(inicio, fin);

        // Assert
        assertThat(respExcel.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respExcel.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("control_ventas_2026_11.xlsx");
        assertThat(respPdfVentas.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respPdfAlertas.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respUsd.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respUsd.getBody()).isNotNull();
    }
}
