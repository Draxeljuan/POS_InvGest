package com.invengest.transaction.domain.usecase;

import com.invengest.transaction.domain.exception.RecursoNoEncontradoException;
import com.invengest.transaction.domain.exception.ReglaNegocioException;
import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.ComprobanteVenta;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.domain.model.gateway.NotificacionGateway;
import com.invengest.transaction.domain.model.gateway.VentaGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComprobanteVentaUseCaseTest {

    @Mock
    private VentaGateway ventaGateway;
    @Mock
    private NotificacionGateway notificacionGateway;

    @InjectMocks
    private ComprobanteVentaUseCase comprobanteVentaUseCase;

    @Test
    @DisplayName("Debe obtener comprobante y enviarlo por correo con email de cliente u override")
    void debeObtenerYEnviarComprobantePorCorreo() {
        // Arrange
        Cliente cliente = new Cliente(1, null, "1001", "Laura", null, "Gómez", null, "300", "laura@correo.com");
        Venta venta = new Venta(12, 1, LocalDateTime.now(), cliente, new MetodoPago(1, "Efectivo"),
                new BigDecimal("12000.00"), Venta.ESTADO_COMPLETADA, null, null, null, List.of());
        when(ventaGateway.findById(12)).thenReturn(Optional.of(venta));

        // Act
        ComprobanteVenta comp = comprobanteVentaUseCase.obtenerComprobante(12);
        ComprobanteVenta enviadoConEmailCliente = comprobanteVentaUseCase.enviarComprobantePorCorreo(12, null);
        ComprobanteVenta enviadoConOverride = comprobanteVentaUseCase.enviarComprobantePorCorreo(12, "alterno@correo.com");

        // Assert
        assertEquals("POS-000012", comp.getNumeroComprobante());
        assertEquals("laura@correo.com", enviadoConEmailCliente.getEmailCliente());
        assertEquals("alterno@correo.com", enviadoConOverride.getEmailCliente());
        verify(notificacionGateway).enviarComprobanteVenta(venta, "laura@correo.com");
        verify(notificacionGateway).enviarComprobanteVenta(venta, "alterno@correo.com");
    }

    @Test
    @DisplayName("Debe lanzar excepciones ante venta inexistente o correo inválido")
    void debeLanzarExcepcionesEnFlujosInvalidos() {
        // Arrange
        Venta ventaSinCorreo = new Venta(13, 1, LocalDateTime.now(), null, new MetodoPago(1, "Efectivo"),
                new BigDecimal("5000.00"), Venta.ESTADO_COMPLETADA, null, null, null, List.of());
        when(ventaGateway.findById(13)).thenReturn(Optional.of(ventaSinCorreo));
        when(ventaGateway.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> comprobanteVentaUseCase.obtenerComprobante(null));
        assertThrows(RecursoNoEncontradoException.class, () -> comprobanteVentaUseCase.obtenerComprobante(999));
        assertThrows(ReglaNegocioException.class, () -> comprobanteVentaUseCase.enviarComprobantePorCorreo(null, "a@b.com"));
        assertThrows(RecursoNoEncontradoException.class, () -> comprobanteVentaUseCase.enviarComprobantePorCorreo(999, "a@b.com"));
        assertThrows(ReglaNegocioException.class, () -> comprobanteVentaUseCase.enviarComprobantePorCorreo(13, null));
        assertThrows(ReglaNegocioException.class, () -> comprobanteVentaUseCase.enviarComprobantePorCorreo(13, "correo-invalido"));
    }
}
