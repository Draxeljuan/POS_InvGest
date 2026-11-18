package com.invengest.analytics.infrastructure.driver_adapters.reports;

import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.TopProducto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfReportAdapterTest {

    @Test
    @DisplayName("Debe generar archivos PDF válidos de ventas y alertas con y sin registros")
    void generarReportesPdf_ConYSinRegistros() {
        // Arrange
        PdfReportAdapter adapter = new PdfReportAdapter();
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);

        List<ResumenVentasDiarias> ventas = List.of(
                ResumenVentasDiarias.builder()
                        .fecha(inicio)
                        .totalVentas(new BigDecimal("100000.00"))
                        .totalAnulado(new BigDecimal("10000.00"))
                        .cantidadOperaciones(5)
                        .build(),
                ResumenVentasDiarias.builder()
                        .fecha(fin)
                        .totalVentas(null)
                        .totalAnulado(null)
                        .cantidadOperaciones(null)
                        .build()
        );
        List<TopProducto> top = List.of(
                new TopProducto(1, "PROD-01", "Resma Carta", 12, inicio)
        );
        List<AlertaStock> alertas = List.of(
                new AlertaStock(1, "PROD-02", "Tijeras", (short) 1, (short) 5, "Bajo Stock", LocalDateTime.now()),
                new AlertaStock(2, null, null, (short) 0, null, "Agotado", null)
        );

        // Act
        byte[] pdfVentas = adapter.generarReporteVentasPdf(inicio, fin, ventas, top);
        byte[] pdfVentasVacio = adapter.generarReporteVentasPdf(inicio, fin, null, null);
        byte[] pdfVentasListaVacia = adapter.generarReporteVentasPdf(inicio, fin, List.of(), List.of());
        byte[] pdfAlertas = adapter.generarReporteAlertasStockPdf(alertas);
        byte[] pdfAlertasVacio = adapter.generarReporteAlertasStockPdf(List.of());
        byte[] pdfAlertasNulo = adapter.generarReporteAlertasStockPdf(null);

        // Assert
        assertThat(new String(pdfVentas, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        assertThat(new String(pdfVentasVacio, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        assertThat(new String(pdfVentasListaVacia, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        assertThat(new String(pdfAlertas, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        assertThat(new String(pdfAlertasVacio, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        assertThat(new String(pdfAlertasNulo, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }
}
