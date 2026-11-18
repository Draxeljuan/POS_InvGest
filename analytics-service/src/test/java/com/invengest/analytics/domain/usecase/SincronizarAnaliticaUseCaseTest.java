package com.invengest.analytics.domain.usecase;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.ItemVentaSincronizacion;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.domain.model.gateway.AlertaStockGateway;
import com.invengest.analytics.domain.model.gateway.ResumenVentaGateway;
import com.invengest.analytics.domain.model.gateway.TopProductoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SincronizarAnaliticaUseCaseTest {

    @Mock
    private ResumenVentaGateway resumenVentaGateway;

    @Mock
    private TopProductoGateway topProductoGateway;

    @Mock
    private AlertaStockGateway alertaStockGateway;

    private SincronizarAnaliticaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SincronizarAnaliticaUseCase(
                resumenVentaGateway,
                topProductoGateway,
                alertaStockGateway
        );
    }

    @Test
    @DisplayName("Debe hacer upsert de resumen diario de ventas y top_productos al sincronizar una venta")
    void registrarVentaSincronizada_UpsertCorrecto() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 8, 10);
        ResumenVentasDiarias existente = ResumenVentasDiarias.builder()
                .fecha(fecha)
                .totalVentas(new BigDecimal("100000.00"))
                .totalAnulado(BigDecimal.ZERO)
                .cantidadOperaciones(5)
                .build();

        when(resumenVentaGateway.findByFecha(fecha)).thenReturn(Optional.of(existente));
        when(resumenVentaGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(topProductoGateway.findByIdProductoAndFecha("PROD-01", fecha)).thenReturn(Optional.empty());
        when(topProductoGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<ItemVentaSincronizacion> items = Arrays.asList(
                new ItemVentaSincronizacion("PROD-01", "Cuaderno", 3),
                new ItemVentaSincronizacion(" ", "Invalido", 2),
                new ItemVentaSincronizacion("PROD-02", "Cero", 0),
                null
        );

        // Act
        ResumenVentasDiarias actualizado = useCase.registrarVentaSincronizada(
                fecha,
                new BigDecimal("45000.00"),
                items
        );
        useCase.registrarVentaSincronizada(null, new BigDecimal("10000.00"), null);

        // Assert
        assertThat(actualizado.getTotalVentas()).isEqualByComparingTo("145000.00");
        assertThat(actualizado.getCantidadOperaciones()).isEqualTo(6);

        ArgumentCaptor<TopProducto> topCaptor = ArgumentCaptor.forClass(TopProducto.class);
        verify(topProductoGateway).save(topCaptor.capture());
        assertThat(topCaptor.getValue().getIdProducto()).isEqualTo("PROD-01");
        assertThat(topCaptor.getValue().getCantidadVendida()).isEqualTo(3);
    }

    @Test
    @DisplayName("Debe incrementar totalAnulado y descontar unidades en top_productos sin valores negativos al anular")
    void registrarAnulacionSincronizada_ReversionSinNegativos() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 8, 10);
        ResumenVentasDiarias existente = ResumenVentasDiarias.builder()
                .fecha(fecha)
                .totalVentas(new BigDecimal("100000.00"))
                .totalAnulado(new BigDecimal("10000.00"))
                .cantidadOperaciones(4)
                .build();
        TopProducto topExistente = TopProducto.builder()
                .idTop(10)
                .idProducto("PROD-01")
                .nombreProducto("Cuaderno")
                .cantidadVendida(2)
                .fecha(fecha)
                .build();

        when(resumenVentaGateway.findByFecha(fecha)).thenReturn(Optional.of(existente));
        when(resumenVentaGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(topProductoGateway.findByIdProductoAndFecha("PROD-01", fecha)).thenReturn(Optional.of(topExistente));
        when(topProductoGateway.findByIdProductoAndFecha("PROD-NO-EXISTE", fecha)).thenReturn(Optional.empty());
        when(topProductoGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ResumenVentasDiarias resultado = useCase.registrarAnulacionSincronizada(
                fecha,
                new BigDecimal("25000.00"),
                Arrays.asList(
                        new ItemVentaSincronizacion("PROD-01", "Cuaderno", 5),
                        new ItemVentaSincronizacion("PROD-NO-EXISTE", "Otro", 1),
                        new ItemVentaSincronizacion(" ", "Vacio", 1),
                        new ItemVentaSincronizacion("PROD-01", "Cero", 0),
                        null
                )
        );
        useCase.registrarAnulacionSincronizada(null, new BigDecimal("5000.00"), null);

        // Assert
        assertThat(resultado.getTotalAnulado()).isEqualByComparingTo("35000.00");
        assertThat(resultado.calcularVentasNetas()).isEqualByComparingTo("65000.00");
        assertThat(resultado.getCantidadOperaciones()).isEqualTo(3);

        ArgumentCaptor<TopProducto> topCaptor = ArgumentCaptor.forClass(TopProducto.class);
        verify(topProductoGateway).save(topCaptor.capture());
        assertThat(topCaptor.getValue().getCantidadVendida()).isZero();
    }

    @Test
    @DisplayName("Debe sincronizar alerta de stock usando stockMinimo real y eliminarla cuando stockActual > stockMinimo")
    void sincronizarAlertaStock_ConStockMinimoReal() {
        // Arrange
        when(alertaStockGateway.findByIdProducto("PROD-02")).thenReturn(Optional.empty());
        when(alertaStockGateway.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Optional<AlertaStock> alertaAgotado = useCase.sincronizarAlertaStock("PROD-02", "Marcador", (short) 0, (short) 3);
        Optional<AlertaStock> alertaBajo = useCase.sincronizarAlertaStock("PROD-02", "Marcador", (short) 3, (short) 3);
        Optional<AlertaStock> resuelta = useCase.sincronizarAlertaStock("PROD-02", "Marcador", (short) 4, (short) 3);

        // Assert
        assertThat(alertaAgotado).isPresent();
        assertThat(alertaAgotado.get().getEstado()).isEqualTo("Agotado");
        assertThat(alertaAgotado.get().getStockMinimo()).isEqualTo((short) 3);
        assertThat(alertaBajo).isPresent();
        assertThat(alertaBajo.get().getEstado()).isEqualTo("Bajo Stock");
        assertThat(resuelta).isEmpty();
        verify(alertaStockGateway).deleteByIdProducto("PROD-02");
    }

    @Test
    @DisplayName("Debe rechazar datos inválidos en sincronización de ventas, anulaciones y alertas")
    void sincronizarAnalitica_ValidacionesDeDominio() {
        // Arrange
        LocalDate hoy = LocalDate.of(2026, 8, 10);

        // Act & Assert
        assertThatThrownBy(() -> useCase.registrarVentaSincronizada(hoy, null, List.of()))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.registrarVentaSincronizada(hoy, new BigDecimal("-5.00"), List.of()))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.registrarAnulacionSincronizada(hoy, null, List.of()))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.registrarAnulacionSincronizada(hoy, new BigDecimal("-1.00"), List.of()))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.sincronizarAlertaStock("", "Prod", (short) 1, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.sincronizarAlertaStock(null, "Prod", (short) 1, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.sincronizarAlertaStock("P1", " ", (short) 1, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.sincronizarAlertaStock("P1", null, (short) 1, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.sincronizarAlertaStock("P1", "Prod", null, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
