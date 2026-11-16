package com.invengest.analytics;

import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.FilaControlVentas;
import com.invengest.analytics.domain.model.ReporteMensualVentas;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.AlertaStockDataGatewayImpl;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.ResumenVentaDataGatewayImpl;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.TopProductoDataGatewayImpl;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.AlertaStockData;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.ResumenVentasDiariasData;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.entity.TopProductoData;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.repository.AlertaStockJpaRepository;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.repository.ResumenVentasDiariasJpaRepository;
import com.invengest.analytics.infrastructure.driver_adapters.base_datos.repository.TopProductoJpaRepository;
import com.invengest.analytics.infrastructure.driver_adapters.reports.ExcelControlVentasAdapter;
import com.invengest.analytics.infrastructure.driver_adapters.reports.PdfReportAdapter;
import com.invengest.analytics.infrastructure.mapper.AlertaStockMapper;
import com.invengest.analytics.infrastructure.mapper.ResumenVentaMapper;
import com.invengest.analytics.infrastructure.mapper.TopProductoMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportGeneratorsAndDataGatewaysTest {

    @Mock
    private ResumenVentasDiariasJpaRepository resumenJpaRepository;

    @Mock
    private TopProductoJpaRepository topProductoJpaRepository;

    @Mock
    private AlertaStockJpaRepository alertaStockJpaRepository;

    @Test
    @DisplayName("ExcelControlVentasAdapter debe generar reporte Excel con 6 columnas RF05 y fila TOTAL MES")
    void excelAdapter_GeneraArchivoConFormatoRF05() throws IOException {
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
                        .build()
        );
        ReporteMensualVentas reporte = ReporteMensualVentas.builder()
                .anio(2026)
                .mes(11)
                .nombreMes("NOVIEMBRE")
                .tituloCabecera("\"VARIEDADES MARELIZA\" NOVIEMBRE 2.026")
                .filas(filas)
                .totalMes(new BigDecimal("120000.00"))
                .build();

        // Act
        byte[] bytes = adapter.generarReporteControlVentas(reporte);

        // Assert
        assertThat(bytes).isNotEmpty();
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("NOVIEMBRE 2026");
            assertThat(sheet).isNotNull();
            Row headerRow = sheet.getRow(1);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("FECHA");
            assertThat(headerRow.getCell(5).getStringCellValue()).isEqualTo("NOTAS");
            Row totalRow = sheet.getRow(3);
            assertThat(totalRow.getCell(0).getStringCellValue()).isEqualTo("TOTAL MES");
            assertThat(totalRow.getCell(1).getNumericCellValue()).isEqualTo(120000.00);
        }
    }

    @Test
    @DisplayName("PdfReportAdapter debe generar archivos PDF válidos de ventas y alertas (con y sin registros)")
    void pdfAdapter_GeneraReportesConYSinRegistros() {
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
                        .build()
        );
        List<TopProducto> top = List.of(
                new TopProducto(1, "PROD-01", "Resma Carta", 12, inicio)
        );
        List<AlertaStock> alertas = List.of(
                new AlertaStock(1, "PROD-02", "Tijeras", (short) 1, (short) 5, "Bajo Stock", LocalDateTime.now())
        );

        // Act
        byte[] pdfVentas = adapter.generarReporteVentasPdf(inicio, fin, ventas, top);
        byte[] pdfVentasVacio = adapter.generarReporteVentasPdf(inicio, fin, List.of(), List.of());
        byte[] pdfAlertas = adapter.generarReporteAlertasStockPdf(alertas);
        byte[] pdfAlertasVacio = adapter.generarReporteAlertasStockPdf(List.of());

        // Assert
        assertThat(new String(pdfVentas, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        assertThat(new String(pdfVentasVacio, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        assertThat(new String(pdfAlertas, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        assertThat(new String(pdfAlertasVacio, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("GatewayImpl de base de datos debe mapear y delegar operaciones JPA correctamente")
    void dataGateways_MapeoYPersistencia() {
        // Arrange
        ResumenVentaMapper resumenMapper = Mappers.getMapper(ResumenVentaMapper.class);
        TopProductoMapper topMapper = Mappers.getMapper(TopProductoMapper.class);
        AlertaStockMapper alertaMapper = Mappers.getMapper(AlertaStockMapper.class);

        ResumenVentaDataGatewayImpl resumenGateway = new ResumenVentaDataGatewayImpl(resumenJpaRepository, resumenMapper);
        TopProductoDataGatewayImpl topGateway = new TopProductoDataGatewayImpl(topProductoJpaRepository, topMapper);
        AlertaStockDataGatewayImpl alertaGateway = new AlertaStockDataGatewayImpl(alertaStockJpaRepository, alertaMapper);

        LocalDate fecha = LocalDate.of(2026, 11, 16);
        ResumenVentasDiariasData resumenEntity = new ResumenVentasDiariasData(
                fecha, new BigDecimal("80000.00"), BigDecimal.ZERO, 3
        );
        TopProductoData topEntity = new TopProductoData(1, "PROD-01", "Cuaderno", 10, fecha);
        AlertaStockData alertaEntity = new AlertaStockData(
                1, "PROD-01", "Cuaderno", (short) 2, (short) 5, "Bajo Stock", LocalDateTime.now()
        );

        when(resumenJpaRepository.findById(fecha)).thenReturn(Optional.of(resumenEntity));
        when(resumenJpaRepository.findByFechaBetweenOrderByFechaAsc(fecha, fecha)).thenReturn(List.of(resumenEntity));
        when(resumenJpaRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(resumenEntity));
        when(resumenJpaRepository.save(any())).thenReturn(resumenEntity);

        when(topProductoJpaRepository.findByFechaOrderByCantidadVendidaDesc(fecha)).thenReturn(List.of(topEntity));
        when(topProductoJpaRepository.findByFechaBetweenOrderByCantidadVendidaDesc(fecha, fecha)).thenReturn(List.of(topEntity));
        when(topProductoJpaRepository.findTopGroupedByFechaBetween(any(), any(), any())).thenReturn(List.of(topEntity));
        when(topProductoJpaRepository.findFirstByIdProductoAndFecha("PROD-01", fecha)).thenReturn(Optional.of(topEntity));
        when(topProductoJpaRepository.save(any())).thenReturn(topEntity);

        when(alertaStockJpaRepository.findAllByOrderByFechaAlertaDesc()).thenReturn(List.of(alertaEntity));
        when(alertaStockJpaRepository.findByEstadoOrderByFechaAlertaDesc("Bajo Stock")).thenReturn(List.of(alertaEntity));
        when(alertaStockJpaRepository.findAllByOrderByFechaAlertaDesc(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of(alertaEntity));
        when(alertaStockJpaRepository.findFirstByIdProducto("PROD-01")).thenReturn(Optional.of(alertaEntity));
        when(alertaStockJpaRepository.save(any())).thenReturn(alertaEntity);

        // Act & Assert
        assertThat(resumenGateway.findByFecha(fecha)).isPresent();
        assertThat(resumenGateway.findByFechaBetween(fecha, fecha)).hasSize(1);
        assertThat(resumenGateway.findAll()).hasSize(1);
        assertThat(resumenGateway.save(ResumenVentasDiarias.crearVacio(fecha)).getTotalVentas())
                .isEqualByComparingTo("80000.00");

        assertThat(topGateway.findByFecha(fecha)).hasSize(1);
        assertThat(topGateway.findByFechaBetween(fecha, fecha)).hasSize(1);
        assertThat(topGateway.findTopNByFechaBetween(fecha, fecha, 5)).hasSize(1);
        assertThat(topGateway.findByIdProductoAndFecha("PROD-01", fecha)).isPresent();
        assertThat(topGateway.save(TopProducto.crearInicial("PROD-01", "Cuaderno", fecha)).getCantidadVendida())
                .isEqualTo(10);

        assertThat(alertaGateway.findAll()).hasSize(1);
        assertThat(alertaGateway.findByEstado("Bajo Stock")).hasSize(1);
        assertThat(alertaGateway.findRecentAlerts(5)).hasSize(1);
        assertThat(alertaGateway.findByIdProducto("PROD-01")).isPresent();
        assertThat(alertaGateway.save(new AlertaStock()).getStockMinimo()).isEqualTo((short) 5);
        alertaGateway.deleteByIdProducto("PROD-01");
        verify(alertaStockJpaRepository).deleteByIdProducto("PROD-01");
    }
}
