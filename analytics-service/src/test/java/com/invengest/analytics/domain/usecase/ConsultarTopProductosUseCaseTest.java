package com.invengest.analytics.domain.usecase;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.domain.model.gateway.TopProductoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarTopProductosUseCaseTest {

    @Mock
    private TopProductoGateway topProductoGateway;

    private ConsultarTopProductosUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarTopProductosUseCase(topProductoGateway);
    }

    @Test
    @DisplayName("Debe obtener top productos por fecha y por rango con límite explícito o por defecto")
    void obtenerPorFechaYRango_Exitoso() {
        // Arrange
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 16);
        when(topProductoGateway.findByFecha(fin)).thenReturn(List.of(new TopProducto(1, "P1", "Prod", 4, fin)));
        when(topProductoGateway.findByFecha(LocalDate.now())).thenReturn(List.of());
        when(topProductoGateway.findTopNByFechaBetween(inicio, fin, 5)).thenReturn(List.of());
        when(topProductoGateway.findTopNByFechaBetween(inicio, fin, 10)).thenReturn(List.of());

        // Act
        List<TopProducto> porFecha = useCase.obtenerPorFecha(fin);
        List<TopProducto> porFechaNula = useCase.obtenerPorFecha(null);
        List<TopProducto> porRango = useCase.obtenerTopPorRango(inicio, fin, 5);
        List<TopProducto> porRangoDefecto = useCase.obtenerTopPorRango(inicio, fin, 0);
        List<TopProducto> porRangoNulo = useCase.obtenerTopPorRango(inicio, fin, null);

        // Assert
        assertThat(porFecha).hasSize(1);
        assertThat(porFechaNula).isEmpty();
        assertThat(porRango).isEmpty();
        assertThat(porRangoDefecto).isEmpty();
        assertThat(porRangoNulo).isEmpty();
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException cuando las fechas de inicio o fin son nulas o invertidas")
    void obtenerTopPorRango_FechasInvalidas_LanzaExcepcion() {
        // Arrange
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 16);

        // Act & Assert
        assertThatThrownBy(() -> useCase.obtenerTopPorRango(null, fin, 5))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.obtenerTopPorRango(inicio, null, 5))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> useCase.obtenerTopPorRango(fin, inicio, 5))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
