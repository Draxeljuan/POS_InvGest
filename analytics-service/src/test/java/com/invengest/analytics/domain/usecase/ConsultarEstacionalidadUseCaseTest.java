package com.invengest.analytics.domain.usecase;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.DiaFestivo;
import com.invengest.analytics.domain.model.ReporteEstacionalidad;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.gateway.CalendarioFestivosGateway;
import com.invengest.analytics.domain.model.gateway.ResumenVentaGateway;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarEstacionalidadUseCaseTest {

    @Mock
    private ResumenVentaGateway resumenVentaGateway;

    @Mock
    private CalendarioFestivosGateway calendarioFestivosGateway;

    private ConsultarEstacionalidadUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarEstacionalidadUseCase(resumenVentaGateway, calendarioFestivosGateway);
    }

    @Test
    @DisplayName("Debe analizar estacionalidad mensual consultando ventas del mes y festivos del año")
    void analizarEstacionalidadMensual_Exitoso() {
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
        when(calendarioFestivosGateway.obtenerFestivos(2026)).thenReturn(List.of(
                new DiaFestivo(LocalDate.of(2026, 11, 16), "Independencia de Cartagena", DiaFestivo.FUENTE_API_LIVE)
        ));

        // Act
        ReporteEstacionalidad estacionalidad = useCase.analizarEstacionalidadMensual(2026, 11);

        // Assert
        assertThat(estacionalidad.getVentasDiasNoHabiles()).isEqualByComparingTo("350000.00");
        assertThat(estacionalidad.getOperacionesDiasNoHabiles()).isEqualTo(7);
    }

    @Test
    @DisplayName("Debe rechazar año o mes fuera de rango en análisis de estacionalidad")
    void analizarEstacionalidadMensual_ParametrosInvalidos_LanzaExcepcion() {
        // Arrange
        int anioInvalido = 1999;

        // Act & Assert
        assertThatThrownBy(() -> useCase.analizarEstacionalidadMensual(anioInvalido, 11))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.analizarEstacionalidadMensual(2101, 11))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.analizarEstacionalidadMensual(2026, 0))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.analizarEstacionalidadMensual(2026, 13))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
