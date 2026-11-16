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

import java.util.List;
import java.util.UUID;

public class PlantillaCorreoFactory {

    public MensajeCorreo construirBienvenida(DatosBienvenida datos) {
        if (datos == null) {
            throw new ReglaNegocioException("Los datos de bienvenida son obligatorios");
        }
        validarTextoObligatorio(datos.destinatario(), "El correo destinatario es obligatorio");
        validarTextoObligatorio(datos.nombre(), "El nombre del usuario es obligatorio");
        validarTextoObligatorio(datos.username(), "El nombre de usuario (username) es obligatorio");
        validarTextoObligatorio(datos.rol(), "El rol asignado es obligatorio");

        String asunto = "[InvenGest POS] Bienvenido(a) al sistema - " + datos.nombre().trim();
        String cuerpo = String.format(
                """
                Hola %s,

                Tu cuenta en InvenGest POS ha sido creada exitosamente.

                Detalles de acceso:
                - Usuario: %s
                - Rol asignado: %s

                Por seguridad, te recomendamos cambiar tu contraseña en tu primer inicio de sesión.

                Atentamente,
                Equipo InvenGest POS
                """,
                datos.nombre().trim(),
                datos.username().trim(),
                datos.rol().trim()
        );

        return new MensajeCorreo(
                generarIdSolicitud(),
                List.of(datos.destinatario().trim()),
                asunto,
                cuerpo,
                TipoNotificacion.BIENVENIDA
        );
    }

    public MensajeCorreo construirSeguridadCuenta(DatosSeguridadCuenta datos) {
        if (datos == null) {
            throw new ReglaNegocioException("Los datos de seguridad de cuenta son obligatorios");
        }
        validarTextoObligatorio(datos.destinatario(), "El correo destinatario es obligatorio");
        validarTextoObligatorio(datos.nombre(), "El nombre del usuario es obligatorio");
        validarTextoObligatorio(datos.evento(), "El tipo de evento de seguridad es obligatorio");

        String eventoNormalizado = datos.evento().trim().toUpperCase();
        String detalleEvento = switch (eventoNormalizado) {
            case "CAMBIO_CONTRASENA" -> "Se ha actualizado exitosamente la contraseña de tu cuenta.";
            case "RESTABLECIMIENTO" -> {
                validarTextoObligatorio(
                        datos.contrasenaTemporal(),
                        "La contraseña temporal es obligatoria para el evento RESTABLECIMIENTO"
                );
                yield "Un administrador ha restablecido tu acceso. Tu contraseña temporal es: "
                        + datos.contrasenaTemporal().trim();
            }
            case "DESACTIVACION" -> "Tu cuenta de acceso en InvenGest POS ha sido desactivada por un administrador.";
            case "ACTIVACION" -> "Tu cuenta de acceso en InvenGest POS ha sido reactivada.";
            default -> throw new ReglaNegocioException("Evento de seguridad no soportado: " + datos.evento());
        };

        String asunto = "[InvenGest POS] Alerta de Seguridad de Cuenta (" + eventoNormalizado + ")";
        String cuerpo = String.format(
                """
                Hola %s,

                Te informamos sobre una actividad de seguridad en tu cuenta de InvenGest POS:
                %s

                Si no reconoces esta actividad, contacta de inmediato al administrador del sistema.

                Atentamente,
                Seguridad InvenGest POS
                """,
                datos.nombre().trim(),
                detalleEvento
        );

        return new MensajeCorreo(
                generarIdSolicitud(),
                List.of(datos.destinatario().trim()),
                asunto,
                cuerpo,
                TipoNotificacion.SEGURIDAD_CUENTA
        );
    }

    public MensajeCorreo construirComprobanteVenta(DatosComprobanteVenta datos) {
        if (datos == null) {
            throw new ReglaNegocioException("Los datos del comprobante de venta son obligatorios");
        }
        validarTextoObligatorio(datos.destinatario(), "El correo destinatario es obligatorio");
        if (datos.idVenta() == null || datos.idVenta() <= 0) {
            throw new ReglaNegocioException("El identificador de la venta debe ser válido");
        }
        if (datos.items() == null || datos.items().isEmpty()) {
            throw new ReglaNegocioException("El comprobante debe incluir al menos un ítem vendido");
        }
        if (datos.total() == null || datos.total().signum() < 0) {
            throw new ReglaNegocioException("El total de la venta es inválido");
        }
        validarTextoObligatorio(datos.metodoPago(), "El método de pago es obligatorio");

        String cliente = (datos.nombreCliente() != null && !datos.nombreCliente().isBlank())
                ? datos.nombreCliente().trim()
                : "Cliente General";
        String fecha = (datos.fecha() != null && !datos.fecha().isBlank())
                ? datos.fecha().trim()
                : "Fecha registrada en POS";

        StringBuilder lineasItems = new StringBuilder();
        for (ItemComprobante item : datos.items()) {
            lineasItems.append(String.format(
                    "- %s x%d | Unitario: $%s | Subtotal: $%s%n",
                    item.nombre(),
                    item.cantidad(),
                    item.precioUnitario(),
                    item.subtotal()
            ));
        }

        String asunto = "[InvenGest POS] Comprobante de Venta #" + datos.idVenta();
        String cuerpo = String.format(
                """
                Estimado(a) %s,

                Gracias por su compra en nuestra papelería. A continuación el resumen de su comprobante:

                Venta #: %d
                Fecha: %s
                Método de Pago: %s

                Detalle de ítems:
                %s
                TOTAL PAGADO: $%s COP

                Atentamente,
                InvenGest POS
                """,
                cliente,
                datos.idVenta(),
                fecha,
                datos.metodoPago().trim(),
                lineasItems,
                datos.total()
        );

        return new MensajeCorreo(
                generarIdSolicitud(),
                List.of(datos.destinatario().trim()),
                asunto,
                cuerpo,
                TipoNotificacion.COMPROBANTE_VENTA
        );
    }

    public MensajeCorreo construirAlertaStock(DatosAlertaStock datos) {
        if (datos == null) {
            throw new ReglaNegocioException("Los datos de alerta de stock son obligatorios");
        }
        if (datos.destinatarios() == null || datos.destinatarios().isEmpty()) {
            throw new ReglaNegocioException("Debe especificar al menos un destinatario para la alerta de stock");
        }
        if (datos.items() == null || datos.items().isEmpty()) {
            throw new ReglaNegocioException("Debe incluir al menos un producto en alerta de stock");
        }

        StringBuilder lineasProductos = new StringBuilder();
        for (ItemAlertaStock item : datos.items()) {
            lineasProductos.append(String.format(
                    "- [%s] %s | Stock actual: %d | Stock mínimo: %d | Estado: %s%n",
                    item.idProducto(),
                    item.nombre(),
                    item.stockActual(),
                    item.stockMinimo(),
                    item.estado()
            ));
        }

        String asunto = "[InvenGest POS] Alerta de Inventario: " + datos.items().size() + " producto(s) en nivel crítico";
        String cuerpo = String.format(
                """
                Alerta Automática de Inventario — InvenGest POS

                Se han detectado productos en nivel de stock bajo o agotado que requieren reposición:

                %s
                Por favor gestione el reabastecimiento con los proveedores correspondientes.

                Atentamente,
                Módulo de Catálogo e Inventario — InvenGest POS
                """,
                lineasProductos
        );

        return new MensajeCorreo(
                generarIdSolicitud(),
                datos.destinatarios(),
                asunto,
                cuerpo,
                TipoNotificacion.ALERTA_STOCK
        );
    }

    public MensajeCorreo construirCierreCaja(DatosCierreCaja datos) {
        if (datos == null) {
            throw new ReglaNegocioException("Los datos de cierre de caja son obligatorios");
        }
        validarTextoObligatorio(datos.destinatario(), "El correo destinatario es obligatorio");
        validarTextoObligatorio(datos.fecha(), "La fecha del cierre de caja es obligatoria");
        if (datos.esperado() == null || datos.contado() == null || datos.diferencia() == null) {
            throw new ReglaNegocioException("Los montos esperado, contado y diferencia son obligatorios");
        }

        StringBuilder desgloseMetodos = new StringBuilder();
        if (datos.totalesPorMetodo() != null && !datos.totalesPorMetodo().isEmpty()) {
            for (TotalMetodoPago metodo : datos.totalesPorMetodo()) {
                desgloseMetodos.append(String.format("- %s: $%s COP%n", metodo.metodoPago(), metodo.total()));
            }
        } else {
            desgloseMetodos.append("- Sin desglose adicional por método de pago\n");
        }

        String tipoJornada = Boolean.TRUE.equals(datos.esDiaHabil()) ? "Día Hábil" : "Día No Hábil / Festivo";
        String proximoHabil = (datos.proximoDiaHabil() != null && !datos.proximoDiaHabil().isBlank())
                ? datos.proximoDiaHabil().trim()
                : "No especificado";

        String asunto = "[InvenGest POS] Reporte de Cierre de Caja - " + datos.fecha().trim();
        String cuerpo = String.format(
                """
                Reporte de Arqueo y Cierre de Caja — InvenGest POS

                Fecha de cierre: %s (%s)
                Próximo día hábil operativo: %s

                Totales por método de pago:
                %s
                Resumen de Efectivo:
                - Efectivo esperado: $%s COP
                - Efectivo contado:  $%s COP
                - Diferencia:        $%s COP

                Atentamente,
                Módulo de Transacciones POS — InvenGest POS
                """,
                datos.fecha().trim(),
                tipoJornada,
                proximoHabil,
                desgloseMetodos,
                datos.esperado(),
                datos.contado(),
                datos.diferencia()
        );

        return new MensajeCorreo(
                generarIdSolicitud(),
                List.of(datos.destinatario().trim()),
                asunto,
                cuerpo,
                TipoNotificacion.CIERRE_CAJA
        );
    }

    public MensajeCorreo construirPrueba(String destinatario) {
        validarTextoObligatorio(destinatario, "El correo destinatario de prueba es obligatorio");

        String asunto = "[InvenGest POS] Verificación de Canal SMTP";
        String cuerpo = """
                Prueba de conectividad SMTP exitosa.

                El microservicio stateless notification-service de InvenGest POS se encuentra operativo y despachando correos de forma asíncrona (@Async).

                Atentamente,
                InvenGest POS
                """;

        return new MensajeCorreo(
                generarIdSolicitud(),
                List.of(destinatario.trim()),
                asunto,
                cuerpo,
                TipoNotificacion.PRUEBA
        );
    }

    private void validarTextoObligatorio(String valor, String mensajeError) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(mensajeError);
        }
    }

    private String generarIdSolicitud() {
        return UUID.randomUUID().toString();
    }
}
