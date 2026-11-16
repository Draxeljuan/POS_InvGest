package com.invengest.notification.domain.usecase;

import com.invengest.notification.domain.exception.ReglaNegocioException;
import com.invengest.notification.domain.model.DatosAlertaStock;
import com.invengest.notification.domain.model.DatosBienvenida;
import com.invengest.notification.domain.model.DatosCierreCaja;
import com.invengest.notification.domain.model.DatosComprobanteVenta;
import com.invengest.notification.domain.model.DatosSeguridadCuenta;
import com.invengest.notification.domain.model.ItemAlertaStock;
import com.invengest.notification.domain.model.ItemComprobante;
import com.invengest.notification.domain.model.MensajeCorreo;
import com.invengest.notification.domain.model.TipoNotificacion;
import com.invengest.notification.domain.model.TotalMetodoPago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantillaCorreoFactoryTest {

    private PlantillaCorreoFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PlantillaCorreoFactory();
    }

    @Test
    @DisplayName("MensajeCorreo valida sus invariantes obligatorios")
    void mensajeCorreo_DeberiaValidarInvariantes() {
        // Arrange
        List<String> destinatariosValidos = List.of("destino@papeleria.local");

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo(" ", destinatariosValidos, "Asunto", "Cuerpo", TipoNotificacion.PRUEBA));
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo("id-1", List.of(), "Asunto", "Cuerpo", TipoNotificacion.PRUEBA));
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo("id-1", List.of("   "), "Asunto", "Cuerpo", TipoNotificacion.PRUEBA));
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo("id-1", destinatariosValidos, "", "Cuerpo", TipoNotificacion.PRUEBA));
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo("id-1", destinatariosValidos, "Asunto", "  ", TipoNotificacion.PRUEBA));
        assertThrows(ReglaNegocioException.class, () ->
                new MensajeCorreo("id-1", destinatariosValidos, "Asunto", "Cuerpo", null));
    }

    @Test
    @DisplayName("construirBienvenida genera asunto y cuerpo con datos del nuevo usuario")
    void construirBienvenida_DatosValidos_RetornaMensaje() {
        // Arrange
        DatosBienvenida datos = new DatosBienvenida(
                "vendedor@papeleria.local",
                "Laura Gómez",
                "lgomez",
                "Vendedor"
        );

        // Act
        MensajeCorreo resultado = factory.construirBienvenida(datos);

        // Assert
        assertNotNull(resultado.idSolicitud());
        assertEquals(TipoNotificacion.BIENVENIDA, resultado.tipoNotificacion());
        assertEquals(List.of("vendedor@papeleria.local"), resultado.destinatarios());
        assertTrue(resultado.asunto().contains("Laura Gómez"));
        assertTrue(resultado.cuerpo().contains("lgomez"));
        assertTrue(resultado.cuerpo().contains("Vendedor"));
    }

    @Test
    @DisplayName("construirBienvenida lanza ReglaNegocioException cuando faltan datos")
    void construirBienvenida_DatosInvalidos_LanzaExcepcion() {
        // Arrange
        DatosBienvenida sinUsername = new DatosBienvenida("a@b.com", "Nombre", " ", "Vendedor");

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> factory.construirBienvenida(null));
        assertThrows(ReglaNegocioException.class, () -> factory.construirBienvenida(sinUsername));
    }

    @Test
    @DisplayName("construirSeguridadCuenta soporta CAMBIO_CONTRASENA, RESTABLECIMIENTO, DESACTIVACION y ACTIVACION")
    void construirSeguridadCuenta_EventosSoportados_RetornaMensaje() {
        // Arrange
        DatosSeguridadCuenta cambio = new DatosSeguridadCuenta(
                "user@papeleria.local", "Carlos", "CAMBIO_CONTRASENA", null);
        DatosSeguridadCuenta restablecimiento = new DatosSeguridadCuenta(
                "user@papeleria.local", "Carlos", "RESTABLECIMIENTO", "Tmp#2026");
        DatosSeguridadCuenta desactivacion = new DatosSeguridadCuenta(
                "user@papeleria.local", "Carlos", "DESACTIVACION", null);
        DatosSeguridadCuenta activacion = new DatosSeguridadCuenta(
                "user@papeleria.local", "Carlos", "ACTIVACION", null);

        // Act
        MensajeCorreo msgCambio = factory.construirSeguridadCuenta(cambio);
        MensajeCorreo msgRest = factory.construirSeguridadCuenta(restablecimiento);
        MensajeCorreo msgDesact = factory.construirSeguridadCuenta(desactivacion);
        MensajeCorreo msgAct = factory.construirSeguridadCuenta(activacion);

        // Assert
        assertEquals(TipoNotificacion.SEGURIDAD_CUENTA, msgCambio.tipoNotificacion());
        assertTrue(msgRest.cuerpo().contains("Tmp#2026"));
        assertTrue(msgDesact.cuerpo().contains("desactivada"));
        assertTrue(msgAct.cuerpo().contains("reactivada"));
    }

    @Test
    @DisplayName("construirSeguridadCuenta falla si evento es inválido o falta contraseña temporal en RESTABLECIMIENTO")
    void construirSeguridadCuenta_EventosInvalidos_LanzaExcepcion() {
        // Arrange
        DatosSeguridadCuenta sinTemp = new DatosSeguridadCuenta(
                "user@papeleria.local", "Carlos", "RESTABLECIMIENTO", " ");
        DatosSeguridadCuenta eventoDesconocido = new DatosSeguridadCuenta(
                "user@papeleria.local", "Carlos", "OTRO_EVENTO", null);

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> factory.construirSeguridadCuenta(null));
        assertThrows(ReglaNegocioException.class, () -> factory.construirSeguridadCuenta(sinTemp));
        assertThrows(ReglaNegocioException.class, () -> factory.construirSeguridadCuenta(eventoDesconocido));
    }

    @Test
    @DisplayName("construirComprobanteVenta genera detalle de ítems y aplica valores por defecto opcionales")
    void construirComprobanteVenta_DatosValidos_RetornaMensaje() {
        // Arrange
        ItemComprobante item = new ItemComprobante(
                "Cuaderno Norma 100H",
                2,
                new BigDecimal("8500.00"),
                new BigDecimal("17000.00")
        );
        DatosComprobanteVenta datos = new DatosComprobanteVenta(
                "cliente@correo.com",
                null,
                105,
                null,
                List.of(item),
                new BigDecimal("17000.00"),
                "Efectivo"
        );

        // Act
        MensajeCorreo resultado = factory.construirComprobanteVenta(datos);

        // Assert
        assertEquals(TipoNotificacion.COMPROBANTE_VENTA, resultado.tipoNotificacion());
        assertTrue(resultado.asunto().contains("#105"));
        assertTrue(resultado.cuerpo().contains("Cliente General"));
        assertTrue(resultado.cuerpo().contains("Cuaderno Norma 100H"));
        assertTrue(resultado.cuerpo().contains("17000.00"));
    }

    @Test
    @DisplayName("construirComprobanteVenta falla cuando idVenta, items o total son inválidos")
    void construirComprobanteVenta_DatosInvalidos_LanzaExcepcion() {
        // Arrange
        ItemComprobante item = new ItemComprobante("Lapicero", 1, BigDecimal.TEN, BigDecimal.TEN);
        DatosComprobanteVenta sinId = new DatosComprobanteVenta(
                "c@c.com", "Ana", 0, "2026-11-16", List.of(item), BigDecimal.TEN, "Efectivo");
        DatosComprobanteVenta sinItems = new DatosComprobanteVenta(
                "c@c.com", "Ana", 1, "2026-11-16", List.of(), BigDecimal.TEN, "Efectivo");
        DatosComprobanteVenta totalNegativo = new DatosComprobanteVenta(
                "c@c.com", "Ana", 1, "2026-11-16", List.of(item), new BigDecimal("-1"), "Efectivo");

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> factory.construirComprobanteVenta(null));
        assertThrows(ReglaNegocioException.class, () -> factory.construirComprobanteVenta(sinId));
        assertThrows(ReglaNegocioException.class, () -> factory.construirComprobanteVenta(sinItems));
        assertThrows(ReglaNegocioException.class, () -> factory.construirComprobanteVenta(totalNegativo));
    }

    @Test
    @DisplayName("construirAlertaStock incluye múltiples destinatarios y productos críticos")
    void construirAlertaStock_DatosValidos_RetornaMensaje() {
        // Arrange
        ItemAlertaStock item = new ItemAlertaStock("PROD-001", "Resma Carta", 2, 10, "Bajo Stock");
        DatosAlertaStock datos = new DatosAlertaStock(
                List.of("admin1@papeleria.local", "admin2@papeleria.local"),
                List.of(item)
        );

        // Act
        MensajeCorreo resultado = factory.construirAlertaStock(datos);

        // Assert
        assertEquals(TipoNotificacion.ALERTA_STOCK, resultado.tipoNotificacion());
        assertEquals(2, resultado.destinatarios().size());
        assertTrue(resultado.cuerpo().contains("PROD-001"));
        assertTrue(resultado.cuerpo().contains("Resma Carta"));
    }

    @Test
    @DisplayName("construirAlertaStock falla sin destinatarios o sin productos")
    void construirAlertaStock_DatosInvalidos_LanzaExcepcion() {
        // Arrange
        ItemAlertaStock item = new ItemAlertaStock("PROD-001", "Resma", 0, 5, "Agotado");

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> factory.construirAlertaStock(null));
        assertThrows(ReglaNegocioException.class, () ->
                factory.construirAlertaStock(new DatosAlertaStock(List.of(), List.of(item))));
        assertThrows(ReglaNegocioException.class, () ->
                factory.construirAlertaStock(new DatosAlertaStock(List.of("a@b.com"), List.of())));
    }

    @Test
    @DisplayName("construirCierreCaja formatea arqueo en día hábil y día festivo")
    void construirCierreCaja_DatosValidos_RetornaMensaje() {
        // Arrange
        DatosCierreCaja cierreHabil = new DatosCierreCaja(
                "admin@papeleria.local",
                "2026-11-16",
                List.of(new TotalMetodoPago("Efectivo", new BigDecimal("150000.00"))),
                new BigDecimal("150000.00"),
                new BigDecimal("150000.00"),
                BigDecimal.ZERO,
                true,
                "2026-11-17"
        );
        DatosCierreCaja cierreFestivoSinMetodos = new DatosCierreCaja(
                "admin@papeleria.local",
                "2026-11-16",
                List.of(),
                new BigDecimal("100000.00"),
                new BigDecimal("95000.00"),
                new BigDecimal("-5000.00"),
                false,
                null
        );

        // Act
        MensajeCorreo msgHabil = factory.construirCierreCaja(cierreHabil);
        MensajeCorreo msgFestivo = factory.construirCierreCaja(cierreFestivoSinMetodos);

        // Assert
        assertEquals(TipoNotificacion.CIERRE_CAJA, msgHabil.tipoNotificacion());
        assertTrue(msgHabil.cuerpo().contains("Día Hábil"));
        assertTrue(msgHabil.cuerpo().contains("2026-11-17"));
        assertTrue(msgFestivo.cuerpo().contains("Día No Hábil / Festivo"));
        assertTrue(msgFestivo.cuerpo().contains("No especificado"));
    }

    @Test
    @DisplayName("construirCierreCaja lanza excepción si faltan montos obligatorios")
    void construirCierreCaja_DatosInvalidos_LanzaExcepcion() {
        // Arrange
        DatosCierreCaja sinMontos = new DatosCierreCaja(
                "admin@papeleria.local", "2026-11-16", List.of(), null, BigDecimal.ZERO, BigDecimal.ZERO, true, "2026-11-17"
        );

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> factory.construirCierreCaja(null));
        assertThrows(ReglaNegocioException.class, () -> factory.construirCierreCaja(sinMontos));
    }

    @Test
    @DisplayName("construirPrueba genera correo de verificación SMTP")
    void construirPrueba_DestinatarioValido_RetornaMensaje() {
        // Arrange
        String destinatario = "admin@papeleria.local";

        // Act
        MensajeCorreo resultado = factory.construirPrueba(destinatario);

        // Assert
        assertEquals(TipoNotificacion.PRUEBA, resultado.tipoNotificacion());
        assertEquals(List.of("admin@papeleria.local"), resultado.destinatarios());
        assertThrows(ReglaNegocioException.class, () -> factory.construirPrueba("  "));
    }
}
