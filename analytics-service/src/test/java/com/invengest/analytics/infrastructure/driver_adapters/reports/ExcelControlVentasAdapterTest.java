package com.invengest.analytics.infrastructure.driver_adapters.reports;

import com.invengest.analytics.domain.model.FilaControlVentas;
import com.invengest.analytics.domain.model.ReporteMensualVentas;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelControlVentasAdapterTest {

    @Test
    @DisplayName("Debe calentar el motor Apache POI al iniciar y generar reporte Excel con 6 columnas RF05 y fila TOTAL MES")
    void generarReporteControlVentas_ConWarmupYFormatoRF05() throws IOException {
        // Arrange
        ExcelControlVentasAdapter adapter = new ExcelControlVentasAdapter();
        List<FilaControlVentas> filas = List.of(
                FilaControlVentas.builder()
                        .fecha(LocalDate.of(2026, 11, 1))
                        .fechaFormateada("nov-01")
                        .ventasDiarias(new BigDecimal("120000.00"))
                        .acumuladoVentas(new BigDecimal("120000.00"))
                        .salidas("")
                        .controlClases("")
                        .notas("")
                        .build(),
                new FilaControlVentas()
        );
        ReporteMensualVentas reporte = ReporteMensualVentas.builder()
                .anio(2026)
                .mes(11)
                .nombreMes("NOVIEMBRE")
                .tituloCabecera("\"VARIEDADES MARELIZA\" NOVIEMBRE 2.026")
                .filas(filas)
                .totalMes(new BigDecimal("120000.00"))
                .build();
        ReporteMensualVentas reporteSinFilas = ReporteMensualVentas.builder()
                .anio(2026)
                .mes(11)
                .nombreMes("NOVIEMBRE")
                .tituloCabecera("TITULO")
                .filas(null)
                .totalMes(null)
                .build();

        // Act
        adapter.calentarMotorPoiAlIniciar();
        byte[] bytes = adapter.generarReporteControlVentas(reporte);
        byte[] bytesSinFilas = adapter.generarReporteControlVentas(reporteSinFilas);

        // Assert
        assertThat(bytes).isNotEmpty();
        assertThat(bytesSinFilas).isNotEmpty();
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("NOVIEMBRE 2026");
            assertThat(sheet).isNotNull();
            Row headerRow = sheet.getRow(1);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("FECHA");
            assertThat(headerRow.getCell(5).getStringCellValue()).isEqualTo("NOTAS");
            Row totalRow = sheet.getRow(4);
            assertThat(totalRow.getCell(0).getStringCellValue()).isEqualTo("TOTAL MES");
            assertThat(totalRow.getCell(1).getNumericCellValue()).isEqualTo(120000.00);
        }
    }
}
