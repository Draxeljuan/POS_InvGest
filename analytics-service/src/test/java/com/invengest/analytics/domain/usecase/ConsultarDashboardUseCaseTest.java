package com.invengest.analytics.domain.usecase;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.DashboardResumen;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.gateway.AlertaStockGateway;
import com.invengest.analytics.domain.model.gateway.ResumenVentaGateway;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarDashboardUseCaseTest {

    @Mock
    private ResumenVentaGateway resumenVentaGateway;

    @Mock
    private TopProductoGateway topProductoGateway;

    @Mock
    private AlertaStockGateway alertaStockGateway;

    private ConsultarDashboardUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarDashboardUseCase(resumenVentaGateway, topProductoGateway, alertaStockGateway);
    }

    @Test
    @DisplayName("Debe consolidar ventas netas del día, acumulado, top productos y alertas con o sin datos previos")
    void obtenerResumenDashboard_ConDatosYSinDatos() {
        // Arrange
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

        when(resumenVentaGateway.findByFecha(LocalDate.now())).thenReturn(Optional.empty());
        when(resumenVentaGateway.findByFechaBetween(any(), eq(LocalDate.now()))).thenReturn(List.of());

        // Act
        DashboardResumen dashboard = useCase.obtenerResumenDashboard(fecha, 7);
        DashboardResumen dashboardDefecto = useCase.obtenerResumenDashboard(null, 0);

        // Assert
        assertThat(dashboard.getVentasDelDia()).isEqualByComparingTo("150000.00");
        assertThat(dashboard.getVentasAnuladasDelDia()).isEqualByComparingTo("30000.00");
        assertThat(dashboard.getVentasNetasDelDia()).isEqualByComparingTo("120000.00");
        assertThat(dashboard.getVentasAcumuladasPeriodo()).isEqualByComparingTo("120000.00");
        assertThat(dashboard.getOperacionesDelDia()).isEqualTo(5);
        assertThat(dashboardDefecto.getVentasNetasDelDia()).isEqualByComparingTo("0.00");
        assertThat(dashboardDefecto.getOperacionesDelDia()).isZero();
    }

    @Test
    @DisplayName("Debe consultar ventas por rango y rechazar fechas nulas o invertidas")
    void consultarVentasPorRango_ValidaParametros() {
        // Arrange
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 16);
        when(resumenVentaGateway.findByFechaBetween(inicio, fin)).thenReturn(List.of(ResumenVentasDiarias.crearVacio(inicio)));

        // Act
        List<ResumenVentasDiarias> resultado = useCase.consultarVentasPorRango(inicio, fin);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThatThrownBy(() -> useCase.consultarVentasPorRango(fin, inicio))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.consultarVentasPorRango(null, fin))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.consultarVentasPorRango(inicio, null))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
