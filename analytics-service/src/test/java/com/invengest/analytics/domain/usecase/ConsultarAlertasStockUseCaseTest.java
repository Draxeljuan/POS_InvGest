package com.invengest.analytics.domain.usecase;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.gateway.AlertaStockGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarAlertasStockUseCaseTest {

    @Mock
    private AlertaStockGateway alertaStockGateway;

    private ConsultarAlertasStockUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarAlertasStockUseCase(alertaStockGateway);
    }

    @Test
    @DisplayName("Debe obtener todas las alertas, filtrar por estado normalizado y consultar recientes")
    void consultasDeAlertas_Exitoso() {
        // Arrange
        when(alertaStockGateway.findAll()).thenReturn(List.of());
        when(alertaStockGateway.findByEstado("Bajo Stock")).thenReturn(List.of());
        when(alertaStockGateway.findRecentAlerts(5)).thenReturn(List.of());
        when(alertaStockGateway.findRecentAlerts(10)).thenReturn(List.of());

        // Act
        List<AlertaStock> todas = useCase.obtenerTodas();
        List<AlertaStock> porEstado = useCase.obtenerPorEstado("BAJO_STOCK");
        List<AlertaStock> recientesCinco = useCase.obtenerRecientes(5);
        List<AlertaStock> recientesDefecto = useCase.obtenerRecientes(0);
        List<AlertaStock> recientesNulo = useCase.obtenerRecientes(null);

        // Assert
        assertThat(todas).isEmpty();
        assertThat(porEstado).isEmpty();
        assertThat(recientesCinco).isEmpty();
        assertThat(recientesDefecto).isEmpty();
        assertThat(recientesNulo).isEmpty();
    }

    @Test
    @DisplayName("Debe lanzar ReglaNegocioException cuando el estado de alerta es inválido")
    void obtenerPorEstado_Invalido_LanzaExcepcion() {
        // Arrange
        String estadoInvalido = "DESCONOCIDO";

        // Act & Assert
        assertThatThrownBy(() -> useCase.obtenerPorEstado(estadoInvalido))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
