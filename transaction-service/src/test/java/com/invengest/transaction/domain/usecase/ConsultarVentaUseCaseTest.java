package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarVentaUseCaseTest {

    @Mock
    private VentaGateway ventaGateway;

    @InjectMocks
    private ConsultarVentaUseCase consultarVentaUseCase;

    @Test
    @DisplayName("Debe consultar ventas por ID, todas, por usuario y por rango de fechas")
    void debeConsultarVentasExitosamente() {
        // Arrange
        Venta venta = new Venta();
        venta.setIdVenta(1);
        when(ventaGateway.findById(1)).thenReturn(Optional.of(venta));
        when(ventaGateway.findAll()).thenReturn(List.of(venta));
        when(ventaGateway.findByIdUsuario(1)).thenReturn(List.of(venta));
        when(ventaGateway.findByFechaBetween(any(), any())).thenReturn(List.of(venta));

        // Act
        Venta porId = consultarVentaUseCase.obtenerPorId(1);
        List<Venta> todas = consultarVentaUseCase.listarTodas();
        List<Venta> porUsuario = consultarVentaUseCase.listarPorUsuario(1);
        List<Venta> porFechas = consultarVentaUseCase.filtrarPorFechas(LocalDateTime.now().minusDays(1), LocalDateTime.now());

        // Assert
        assertEquals(1, porId.getIdVenta());
        assertEquals(1, todas.size());
        assertEquals(1, porUsuario.size());
        assertEquals(1, porFechas.size());
    }

    @Test
    @DisplayName("Debe validar parámetros obligatorios y existencia de venta en ConsultarVentaUseCase")
    void debeLanzarExcepcionesEnParametrosInvalidos() {
        // Arrange
        when(ventaGateway.findById(99)).thenReturn(Optional.empty());
        LocalDateTime ahora = LocalDateTime.now();

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> consultarVentaUseCase.obtenerPorId(null));
        assertThrows(RecursoNoEncontradoException.class, () -> consultarVentaUseCase.obtenerPorId(99));
        assertThrows(ReglaNegocioException.class, () -> consultarVentaUseCase.listarPorUsuario(null));
        assertThrows(ReglaNegocioException.class, () -> consultarVentaUseCase.filtrarPorFechas(null, ahora));
        assertThrows(ReglaNegocioException.class, () -> consultarVentaUseCase.filtrarPorFechas(ahora, null));
        assertThrows(ReglaNegocioException.class, () -> consultarVentaUseCase.filtrarPorFechas(ahora, ahora.minusDays(1)));
    }
}
