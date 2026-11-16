package com.invengest.analytics;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.DashboardResumen;
import com.invengest.analytics.domain.model.DiaFestivo;
import com.invengest.analytics.domain.model.ReporteEstacionalidad;
import com.invengest.analytics.domain.model.ReporteMensualVentas;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.ResumenVentasUsd;
import com.invengest.analytics.domain.model.TasaCambio;
import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.domain.model.gateway.AlertaStockGateway;
import com.invengest.analytics.domain.model.gateway.CalendarioFestivosGateway;
import com.invengest.analytics.domain.model.gateway.ReporteExcelGateway;
import com.invengest.analytics.domain.model.gateway.ReportePdfGateway;
import com.invengest.analytics.domain.model.gateway.ResumenVentaGateway;
import com.invengest.analytics.domain.model.gateway.TasaCambioGateway;
import com.invengest.analytics.domain.model.gateway.TopProductoGateway;
import com.invengest.analytics.domain.usecase.ConsultarAlertasStockUseCase;
import com.invengest.analytics.domain.usecase.ConsultarDashboardUseCase;
import com.invengest.analytics.domain.usecase.ConsultarEstacionalidadUseCase;
import com.invengest.analytics.domain.usecase.ConsultarTopProductosUseCase;
import com.invengest.analytics.domain.usecase.GenerarReporteControlVentasUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultasYReportesUseCaseTest {

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

    @Mock
    private CalendarioFestivosGateway calendarioFestivosGateway;

    @Test
    @DisplayName("ConsultarDashboardUseCase debe consolidar ventas netas del día, acumulado, top productos y alertas")
    void consultarDashboard_ExitosoYValidaciones() {
        // Arrange
        ConsultarDashboardUseCase useCase = new ConsultarDashboardUseCase(
                resumenVentaGateway, topProductoGateway, alertaStockGateway
        );
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        ResumenVentasDiarias resumenHoy = ResumenVentasDiarias.builder()
                .fecha(fecha)
                .totalVentas(new BigDecimal("150000.00"))
                .totalAnulado(new BigDecimal("30000.00"))
                .cantidadOperaciones(5)
                .build();

        when(resumenVentaGateway.findByFecha(fecha)).thenReturn(Optional.of(resumenHoy));
        when(resumenVentaGateway.findByFechaBetween(fecha.minusDays(6), fecha)).thenReturn(List.of(resumenHoy));
        when(topProductoGateway.findTopNByFechaBetween(fecha.minusDays(6), fecha, 5)).thenReturn(List.of());
        when(alertaStockGateway.findRecentAlerts(10)).thenReturn(List.of());

        // Act
        DashboardResumen dashboard = useCase.obtenerResumenDashboard(fecha, 7);
        List<ResumenVentasDiarias> rango = useCase.consultarVentasPorRango(fecha.minusDays(6), fecha);

        // Assert
        assertThat(dashboard.getVentasDelDia()).isEqualByComparingTo("150000.00");
        assertThat(dashboard.getVentasAnuladasDelDia()).isEqualByComparingTo("30000.00");
        assertThat(dashboard.getVentasNetasDelDia()).isEqualByComparingTo("120000.00");
        assertThat(dashboard.getVentasAcumuladasPeriodo()).isEqualByComparingTo("120000.00");
        assertThat(rango).hasSize(1);

        assertThatThrownBy(() -> useCase.consultarVentasPorRango(fecha, fecha.minusDays(1)))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.consultarVentasPorRango(null, fecha))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("ConsultarTopProductosUseCase y ConsultarAlertasStockUseCase deben filtrar y validar parámetros")
    void consultarTopProductosYAlertas_ExitosoYValidaciones() {
        // Arrange
        ConsultarTopProductosUseCase topUseCase = new ConsultarTopProductosUseCase(topProductoGateway);
        ConsultarAlertasStockUseCase alertasUseCase = new ConsultarAlertasStockUseCase(alertaStockGateway);
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 16);

        when(topProductoGateway.findByFecha(fin)).thenReturn(List.of(new TopProducto(1, "P1", "Prod", 4, fin)));
        when(topProductoGateway.findTopNByFechaBetween(inicio, fin, 5)).thenReturn(List.of());
        when(alertaStockGateway.findAll()).thenReturn(List.of());
        when(alertaStockGateway.findByEstado("Bajo Stock")).thenReturn(List.of());
        when(alertaStockGateway.findRecentAlerts(5)).thenReturn(List.of());

        // Act & Assert
        assertThat(topUseCase.obtenerPorFecha(fin)).hasSize(1);
        assertThat(topUseCase.obtenerTopPorRango(inicio, fin, 5)).isEmpty();
        assertThat(alertasUseCase.obtenerTodas()).isEmpty();
        assertThat(alertasUseCase.obtenerPorEstado("BAJO_STOCK")).isEmpty();
        assertThat(alertasUseCase.obtenerRecientes(5)).isEmpty();

        assertThatThrownBy(() -> topUseCase.obtenerTopPorRango(fin, inicio, 5))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> alertasUseCase.obtenerPorEstado("DESCONOCIDO"))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("GenerarReporteControlVentasUseCase y ConsultarEstacionalidadUseCase deben generar reportes, USD y estacionalidad")
    void generarReportesUsdYEstacionalidad_ExitosoYValidaciones() {
        // Arrange
        GenerarReporteControlVentasUseCase reporteUseCase = new GenerarReporteControlVentasUseCase(
                resumenVentaGateway, topProductoGateway, alertaStockGateway,
                reporteExcelGateway, reportePdfGateway, tasaCambioGateway
        );
        ConsultarEstacionalidadUseCase estacionalidadUseCase = new ConsultarEstacionalidadUseCase(
                resumenVentaGateway, calendarioFestivosGateway
        );

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
        when(calendarioFestivosGateway.obtenerFestivos(2026)).thenReturn(List.of(
                new DiaFestivo(LocalDate.of(2026, 11, 16), "Independencia de Cartagena", DiaFestivo.FUENTE_API_LIVE)
        ));

        // Act
        ReporteMensualVentas datosMensuales = reporteUseCase.construirDatosReporteMensual(2026, 11);
        byte[] excelBytes = reporteUseCase.generarExcelControlVentas(2026, 11);
        byte[] pdfVentas = reporteUseCase.generarPdfVentasPorRango(inicio, fin);
        byte[] pdfAlertas = reporteUseCase.generarPdfAlertasStock();
        ResumenVentasUsd resumenUsd = reporteUseCase.obtenerResumenVentasUsd(inicio, fin);
        ReporteEstacionalidad estacionalidad = estacionalidadUseCase.analizarEstacionalidadMensual(2026, 11);

        // Assert
        assertThat(datosMensuales.getFilas()).hasSize(30);
        assertThat(datosMensuales.getTotalMes()).isEqualByComparingTo("350000.00");
        assertThat(excelBytes).containsExactly(1, 2, 3);
        assertThat(pdfVentas).containsExactly(4, 5);
        assertThat(pdfAlertas).containsExactly(6, 7);
        assertThat(resumenUsd.getTotalNetoCop()).isEqualByComparingTo("350000.00");
        assertThat(resumenUsd.getTotalNetoUsd()).isEqualByComparingTo("100.00");
        assertThat(estacionalidad.getVentasDiasNoHabiles()).isEqualByComparingTo("350000.00");

        assertThatThrownBy(() -> reporteUseCase.generarExcelControlVentas(2026, 13))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> reporteUseCase.obtenerResumenVentasUsd(fin, inicio))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> estacionalidadUseCase.analizarEstacionalidadMensual(1999, 11))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
