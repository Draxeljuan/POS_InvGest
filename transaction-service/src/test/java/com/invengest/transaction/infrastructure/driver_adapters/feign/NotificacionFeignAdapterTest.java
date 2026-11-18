package com.invengest.transaction.infrastructure.driver_adapters.feign;

import com.invengest.transaction.domain.model.CierreCaja;
import com.invengest.transaction.domain.model.CierreCajaDetalle;
import com.invengest.transaction.domain.model.Cliente;
import com.invengest.transaction.domain.model.DetalleVenta;
import com.invengest.transaction.domain.model.MetodoPago;
import com.invengest.transaction.domain.model.TipoItem;
import com.invengest.transaction.domain.model.Venta;
import com.invengest.transaction.infrastructure.driver_adapters.feign.dto.ContactoUsuarioFeignDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionFeignAdapterTest {

    @Mock
    private NotificationFeignClient notifClient;
    @Mock
    private AuthFeignClient authClient;

    @InjectMocks
    private NotificacionFeignAdapter notifAdapter;

    @Test
    @DisplayName("Debe enviar comprobante de venta y resumen de cierre de caja aislando errores SMTP/Feign")
    void debeEnviarComprobanteYResumenCierreAislandoErrores() {
        // Arrange
        when(authClient.obtenerContactosAdministradores()).thenReturn(List.of(
                new ContactoUsuarioFeignDTO(1, "Admin", "admin@test.com"),
                new ContactoUsuarioFeignDTO(2, "SinMail", "  ")
        ));
        doThrow(new RuntimeException("SMTP down")).when(notifClient).enviarComprobanteVenta(any());
        doThrow(new RuntimeException("SMTP down")).when(notifClient).enviarResumenCierreCaja(any());

        Venta venta = new Venta(1, 1, LocalDateTime.now(),
                new Cliente(1, null, "100", "Ana", null, "Gil", null, "300", "ana@test.com"),
                new MetodoPago(1, "Efectivo"), new BigDecimal("5000.00"),
                Venta.ESTADO_COMPLETADA, null, null, null,
                List.of(new DetalleVenta(1, 1, "P1", TipoItem.PRODUCTO, "Cuaderno", new BigDecimal("5000.00"), 1, new BigDecimal("5000.00"))));

        Venta ventaAnonima = new Venta();
        ventaAnonima.setDetalles(null);

        CierreCaja cierre = new CierreCaja(1, LocalDate.now(), 1, new BigDecimal("5000.00"), 1,
                new BigDecimal("5000.00"), new BigDecimal("5000.00"), BigDecimal.ZERO, true, LocalDate.now().plusDays(1),
                "OK", LocalDateTime.now(), List.of(new CierreCajaDetalle(1, 1, new MetodoPago(1, "Efectivo"), new BigDecimal("5000.00"), 1)));
        CierreCaja cierreSinDet = new CierreCaja();
        cierreSinDet.setDetalles(null);

        // Act & Assert
        assertDoesNotThrow(() -> notifAdapter.enviarComprobanteVenta(null, null));
        assertDoesNotThrow(() -> notifAdapter.enviarComprobanteVenta(ventaAnonima, null));
        assertDoesNotThrow(() -> notifAdapter.enviarComprobanteVenta(ventaAnonima, "custom@test.com"));
        assertDoesNotThrow(() -> notifAdapter.enviarComprobanteVenta(venta, null));

        assertDoesNotThrow(() -> notifAdapter.enviarResumenCierreCaja(null));
        assertDoesNotThrow(() -> notifAdapter.enviarResumenCierreCaja(cierre));
        when(authClient.obtenerContactosAdministradores()).thenReturn(List.of());
        assertDoesNotThrow(() -> notifAdapter.enviarResumenCierreCaja(cierreSinDet));
    }
}
