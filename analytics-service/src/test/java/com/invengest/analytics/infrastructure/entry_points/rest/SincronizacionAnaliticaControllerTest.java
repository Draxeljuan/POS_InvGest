package com.invengest.analytics.infrastructure.entry_points.rest;

import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.ItemVentaSincronizacion;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.usecase.SincronizarAnaliticaUseCase;
import com.invengest.analytics.infrastructure.entry_points.dto.request.ItemVentaSincronizacionRequestDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.request.SincronizarAlertaStockRequestDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.request.SincronizarAnulacionVentaRequestDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.request.SincronizarVentaRequestDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.response.AlertaStockResponseDTO;
import com.invengest.analytics.infrastructure.entry_points.dto.response.ResumenVentaResponseDTO;
import com.invengest.analytics.infrastructure.mapper.AlertaStockMapper;
import com.invengest.analytics.infrastructure.mapper.AnaliticaRestMapper;
import com.invengest.analytics.infrastructure.mapper.ResumenVentaMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SincronizacionAnaliticaControllerTest {

    @Mock
    private SincronizarAnaliticaUseCase sincronizarAnaliticaUseCase;

    @Mock
    private ResumenVentaMapper resumenVentaMapper;

    @Mock
    private AlertaStockMapper alertaStockMapper;

    @Mock
    private AnaliticaRestMapper analiticaRestMapper;

    @InjectMocks
    private SincronizacionAnaliticaController controller;

    @Test
    @DisplayName("Debe sincronizar venta con o sin lista de ítems y devolver 200 OK")
    void sincronizarVenta_ConYSinItems() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        SincronizarVentaRequestDTO reqConItems = SincronizarVentaRequestDTO.builder()
                .fecha(fecha)
                .totalVenta(new BigDecimal("100000.00"))
                .items(List.of(ItemVentaSincronizacionRequestDTO.builder().idProducto("P1").cantidadVendida(2).build()))
                .build();
        SincronizarVentaRequestDTO reqSinItems = SincronizarVentaRequestDTO.builder()
                .fecha(fecha)
                .totalVenta(new BigDecimal("50000.00"))
                .items(null)
                .build();

        ResumenVentasDiarias resumen = ResumenVentasDiarias.crearVacio(fecha);
        ResumenVentaResponseDTO dto = ResumenVentaResponseDTO.builder().fecha(fecha).build();

        when(analiticaRestMapper.toDomainItems(any())).thenReturn(List.of(new ItemVentaSincronizacion("P1", "Prod", 2)));
        when(sincronizarAnaliticaUseCase.registrarVentaSincronizada(eq(fecha), any(), any())).thenReturn(resumen);
        when(resumenVentaMapper.toResponse(resumen)).thenReturn(dto);

        // Act
        ResponseEntity<ResumenVentaResponseDTO> respConItems = controller.sincronizarVenta(reqConItems);
        ResponseEntity<ResumenVentaResponseDTO> respSinItems = controller.sincronizarVenta(reqSinItems);

        // Assert
        assertThat(respConItems.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respSinItems.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Debe sincronizar anulación de venta con o sin lista de ítems y devolver 200 OK")
    void sincronizarAnulacionVenta_ConYSinItems() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        SincronizarAnulacionVentaRequestDTO reqConItems = SincronizarAnulacionVentaRequestDTO.builder()
                .fechaVenta(fecha)
                .totalAnulado(new BigDecimal("20000.00"))
                .items(List.of(ItemVentaSincronizacionRequestDTO.builder().idProducto("P1").cantidadAnulada(1).build()))
                .build();
        SincronizarAnulacionVentaRequestDTO reqSinItems = SincronizarAnulacionVentaRequestDTO.builder()
                .fecha(fecha)
                .totalAnulado(new BigDecimal("10000.00"))
                .items(null)
                .build();

        ResumenVentasDiarias resumen = ResumenVentasDiarias.crearVacio(fecha);
        ResumenVentaResponseDTO dto = ResumenVentaResponseDTO.builder().fecha(fecha).build();

        when(analiticaRestMapper.toDomainItems(any())).thenReturn(List.of(new ItemVentaSincronizacion("P1", "Prod", 1)));
        when(sincronizarAnaliticaUseCase.registrarAnulacionSincronizada(eq(fecha), any(), any())).thenReturn(resumen);
        when(resumenVentaMapper.toResponse(resumen)).thenReturn(dto);

        // Act
        ResponseEntity<ResumenVentaResponseDTO> respConItems = controller.sincronizarAnulacionVenta(reqConItems);
        ResponseEntity<ResumenVentaResponseDTO> respSinItems = controller.sincronizarAnulacionVenta(reqSinItems);

        // Assert
        assertThat(respConItems.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respSinItems.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Debe devolver 200 OK cuando se genera alerta de stock y 204 No Content cuando se resuelve")
    void sincronizarAlertaStock_ActivaYResuelta() {
        // Arrange
        SincronizarAlertaStockRequestDTO reqActiva = SincronizarAlertaStockRequestDTO.builder()
                .idProducto("P1")
                .nombreProducto("Cuaderno")
                .stockActual((short) 2)
                .stockMinimo((short) 5)
                .build();
        SincronizarAlertaStockRequestDTO reqResuelta = SincronizarAlertaStockRequestDTO.builder()
                .idProducto("P1")
                .nombreProducto("Cuaderno")
                .stockActual((short) 10)
                .stockMinimo((short) 5)
                .build();

        AlertaStock alerta = AlertaStock.builder().idProducto("P1").build();
        AlertaStockResponseDTO dto = AlertaStockResponseDTO.builder().idProducto("P1").build();

        when(sincronizarAnaliticaUseCase.sincronizarAlertaStock("P1", "Cuaderno", (short) 2, (short) 5))
                .thenReturn(Optional.of(alerta));
        when(alertaStockMapper.toResponse(alerta)).thenReturn(dto);
        when(sincronizarAnaliticaUseCase.sincronizarAlertaStock("P1", "Cuaderno", (short) 10, (short) 5))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<AlertaStockResponseDTO> respActiva = controller.sincronizarAlertaStock(reqActiva);
        ResponseEntity<AlertaStockResponseDTO> respResuelta = controller.sincronizarAlertaStock(reqResuelta);

        // Assert
        assertThat(respActiva.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respResuelta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
