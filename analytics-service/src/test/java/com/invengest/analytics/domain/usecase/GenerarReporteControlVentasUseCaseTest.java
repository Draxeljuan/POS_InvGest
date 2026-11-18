package com.invengest.analytics.domain.usecase;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.ReporteMensualVentas;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.ResumenVentasUsd;
import com.invengest.analytics.domain.model.TasaCambio;
import com.invengest.analytics.domain.model.gateway.AlertaStockGateway;
import com.invengest.analytics.domain.model.gateway.ReporteExcelGateway;
import com.invengest.analytics.domain.model.gateway.ReportePdfGateway;
import com.invengest.analytics.domain.model.gateway.ResumenVentaGateway;
import com.invengest.analytics.domain.model.gateway.TasaCambioGateway;
import com.invengest.analytics.domain.model.gateway.TopProductoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerarReporteControlVentasUseCaseTest {

    @Mock
    private ResumenVentaGateway resumenVentaGateway;

    @Mock
    private TopProductoGateway topProductoGateway;

    @Mock
    private AlertaStockGateway alertaStockGateway;

    @Mock
    private ReporteExcelGateway reporteExcelGateway;

    @Mock
    private ReportePdfGateway reportePdfGateway;

    @Mock
    private TasaCambioGateway tasaCambioGateway;

    private GenerarReporteControlVentasUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerarReporteControlVentasUseCase(
                resumenVentaGateway,
                topProductoGateway,
                alertaStockGateway,
                reporteExcelGateway,
                reportePdfGateway,
                tasaCambioGateway
        );
    }

    @Test
    @DisplayName("Debe construir datos mensuales, generar Excel/PDF y convertir resumen de ventas a USD")
    void generarReportesYResumenUsd_Exitoso() {
        // Arrange
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);
        List<ResumenVentasDiarias> resumenes = List.of(
                ResumenVentasDiarias.builder()
                        .fecha(LocalDate.of(2026, 11, 16))
                        .totalVentas(new BigDecimal("400000.00"))
                        .totalAnulado(new BigDecimal("50000.00"))
                        .cantidadOperaciones(7)
                        .build()
        );

        when(resumenVentaGateway.findByFechaBetween(inicio, fin)).thenReturn(resumenes);
        when(reporteExcelGateway.generarReporteControlVentas(any())).thenReturn(new byte[]{1, 2, 3});
        when(reportePdfGateway.generarReporteVentasPdf(eq(inicio), eq(fin), eq(resumenes), any())).thenReturn(new byte[]{4, 5});
        when(reportePdfGateway.generarReporteAlertasStockPdf(any())).thenReturn(new byte[]{6, 7});
        when(tasaCambioGateway.obtenerTrmVigente()).thenReturn(
                new TasaCambio(new BigDecimal("3500.00"), LocalDate.of(2026, 11, 16), TasaCambio.FUENTE_API_LIVE)
        );

        // Act
        ReporteMensualVentas datosMensuales = useCase.construirDatosReporteMensual(2026, 11);
        byte[] excelBytes = useCase.generarExcelControlVentas(2026, 11);
        byte[] pdfVentas = useCase.generarPdfVentasPorRango(inicio, fin);
        byte[] pdfAlertas = useCase.generarPdfAlertasStock();
        ResumenVentasUsd resumenUsd = useCase.obtenerResumenVentasUsd(inicio, fin);

        // Assert
        assertThat(datosMensuales.getFilas()).hasSize(30);
        assertThat(datosMensuales.getTotalMes()).isEqualByComparingTo("350000.00");
        assertThat(excelBytes).containsExactly(1, 2, 3);
        assertThat(pdfVentas).containsExactly(4, 5);
        assertThat(pdfAlertas).containsExactly(6, 7);
        assertThat(resumenUsd.getTotalNetoCop()).isEqualByComparingTo("350000.00");
        assertThat(resumenUsd.getTotalNetoUsd()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Debe validar año, mes y rangos de fechas en la generación de reportes y resumen USD")
    void validaciones_ParametrosInvalidos_LanzaExcepcion() {
        // Arrange
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);

        // Act & Assert
        assertThatThrownBy(() -> useCase.generarExcelControlVentas(2026, 0))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.generarExcelControlVentas(2026, 13))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.generarExcelControlVentas(1999, 11))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.generarExcelControlVentas(2101, 11))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.generarPdfVentasPorRango(null, fin))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.generarPdfVentasPorRango(inicio, null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.generarPdfVentasPorRango(fin, inicio))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.obtenerResumenVentasUsd(null, fin))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.obtenerResumenVentasUsd(inicio, null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.obtenerResumenVentasUsd(fin, inicio))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
