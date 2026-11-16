package com.invengest.analytics;

import com.invengest.analytics.domain.exception.ReglaNegocioException;
import com.invengest.analytics.domain.model.AlertaStock;
import com.invengest.analytics.domain.model.CalendarioLaboral;
import com.invengest.analytics.domain.model.DiaFestivo;
import com.invengest.analytics.domain.model.ReporteEstacionalidad;
import com.invengest.analytics.domain.model.ResumenVentasDiarias;
import com.invengest.analytics.domain.model.ResumenVentasUsd;
import com.invengest.analytics.domain.model.TasaCambio;
import com.invengest.analytics.domain.model.TopProducto;
import com.invengest.analytics.domain.model.gateway.CalendarioFestivosGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainModelsAndCalendarioLaboralTest {

    @Test
    @DisplayName("ResumenVentasDiarias debe registrar ventas, anulaciones y calcular ventas netas sin valores negativos")
    void resumenVentasDiarias_ComportamientoPuro() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        ResumenVentasDiarias resumen = ResumenVentasDiarias.crearVacio(fecha);
        ResumenVentasDiarias resumenDesdeNulos = new ResumenVentasDiarias();
        ResumenVentasDiarias resumenFechaNula = ResumenVentasDiarias.crearVacio(null);

        // Act
        resumen.registrarVenta(new BigDecimal("120000.00"));
        resumen.registrarVenta(new BigDecimal("30000.00"));
        resumen.registrarAnulacion(new BigDecimal("50000.00"));
        BigDecimal netas = resumen.calcularVentasNetas();

        resumenDesdeNulos.registrarVenta(new BigDecimal("10000.00"));
        ResumenVentasDiarias otroNulo = new ResumenVentasDiarias();
        otroNulo.registrarAnulacion(new BigDecimal("5000.00"));

        // Assert
        assertThat(resumenFechaNula.getFecha()).isNotNull();
        assertThat(resumenDesdeNulos.calcularVentasNetas()).isEqualByComparingTo("10000.00");
        assertThat(otroNulo.calcularVentasNetas()).isEqualByComparingTo("0.00");
        assertThat(new ResumenVentasDiarias().calcularVentasNetas()).isEqualByComparingTo("0.00");
        assertThat(resumen.getTotalVentas()).isEqualByComparingTo("150000.00");
        assertThat(resumen.getTotalAnulado()).isEqualByComparingTo("50000.00");
        assertThat(resumen.getCantidadOperaciones()).isEqualTo(1);
        assertThat(netas).isEqualByComparingTo("100000.00");

        // Act & Assert (clamping y validaciones)
        resumen.registrarAnulacion(new BigDecimal("200000.00"));
        resumen.registrarAnulacion(new BigDecimal("10000.00"));
        assertThat(resumen.getCantidadOperaciones()).isZero();
        assertThat(resumen.calcularVentasNetas()).isEqualByComparingTo("0.00");

        assertThatThrownBy(() -> resumen.registrarVenta(null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> resumen.registrarVenta(new BigDecimal("-1.00")))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> resumen.registrarAnulacion(null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> resumen.registrarAnulacion(new BigDecimal("-1.00")))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("TopProducto debe incrementar unidades vendidas y descontar anulaciones sin bajar de cero")
    void topProducto_ComportamientoPuro() {
        // Arrange
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        TopProducto top = TopProducto.crearInicial("PROD-001", "Cuaderno Argollado", fecha);
        TopProducto topSinNombreYFecha = TopProducto.crearInicial("PROD-002", " ", null);
        TopProducto topVacio = new TopProducto();

        // Act
        top.incrementarCantidad(5, "Cuaderno Argollado 100H");
        top.incrementarCantidad(1, " ");
        top.descontarAnulacion(2);
        topVacio.incrementarCantidad(3, null);
        new TopProducto().descontarAnulacion(1);

        // Assert
        assertThat(topSinNombreYFecha.getNombreProducto()).isEqualTo("PROD-002");
        assertThat(topSinNombreYFecha.getFecha()).isNotNull();
        assertThat(top.getCantidadVendida()).isEqualTo(4);
        assertThat(top.getNombreProducto()).isEqualTo("Cuaderno Argollado 100H");
        assertThat(topVacio.getCantidadVendida()).isEqualTo(3);

        // Act & Assert (clamping en cero y validaciones)
        top.descontarAnulacion(10);
        assertThat(top.getCantidadVendida()).isZero();

        assertThatThrownBy(() -> TopProducto.crearInicial(null, "Nombre", fecha))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> TopProducto.crearInicial(" ", "Nombre", fecha))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> top.incrementarCantidad(0, "Nombre"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> top.descontarAnulacion(0))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("AlertaStock debe evaluar umbral real stockMinimo y normalizar estados de filtro")
    void alertaStock_ComportamientoPuro() {
        // Arrange
        AlertaStock alerta = AlertaStock.builder().idProducto("PROD-010").build();

        // Act
        boolean requiereBajo = AlertaStock.requiereAlerta((short) 4, (short) 5);
        boolean requiereAgotado = AlertaStock.requiereAlerta((short) 0, (short) 0);
        boolean requiereNormal = AlertaStock.requiereAlerta((short) 6, (short) 5);
        boolean requiereDefecto = AlertaStock.requiereAlerta((short) 9, null);
        alerta.actualizarDatosAlerta("Lapicero Negro", (short) 0, (short) 5);

        // Assert
        assertThat(requiereBajo).isTrue();
        assertThat(requiereAgotado).isTrue();
        assertThat(requiereNormal).isFalse();
        assertThat(requiereDefecto).isTrue();
        assertThat(alerta.getEstado()).isEqualTo(AlertaStock.ESTADO_AGOTADO);
        assertThat(alerta.getStockMinimo()).isEqualTo((short) 5);
        assertThat(AlertaStock.normalizarEstadoFiltro("BAJO_STOCK")).isEqualTo(AlertaStock.ESTADO_BAJO_STOCK);
        assertThat(AlertaStock.normalizarEstadoFiltro("Bajo Stock")).isEqualTo(AlertaStock.ESTADO_BAJO_STOCK);
        assertThat(AlertaStock.normalizarEstadoFiltro("Agotado")).isEqualTo(AlertaStock.ESTADO_AGOTADO);
        assertThat(AlertaStock.normalizarEstadoFiltro("AGOTADO")).isEqualTo(AlertaStock.ESTADO_AGOTADO);

        assertThatThrownBy(() -> AlertaStock.requiereAlerta(null, (short) 5))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> AlertaStock.evaluarEstado(null, (short) 5))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> AlertaStock.normalizarEstadoFiltro(null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> AlertaStock.normalizarEstadoFiltro(" "))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> AlertaStock.normalizarEstadoFiltro("INVALIDO"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> new AlertaStock().actualizarDatosAlerta("Prod", (short) 1, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> alerta.actualizarDatosAlerta(" ", (short) 1, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> alerta.actualizarDatosAlerta("Prod", null, (short) 2))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("TasaCambio y ResumenVentasUsd deben convertir totales vendidos, anulados y netos a USD")
    void resumenVentasUsd_ConversionCorrecta() {
        // Arrange
        TasaCambio trm = new TasaCambio(new BigDecimal("4000.00"), null, " ");
        List<ResumenVentasDiarias> resumenes = List.of(
                ResumenVentasDiarias.builder()
                        .fecha(LocalDate.of(2026, 11, 15))
                        .totalVentas(new BigDecimal("200000.00"))
                        .totalAnulado(new BigDecimal("40000.00"))
                        .cantidadOperaciones(4)
                        .build(),
                ResumenVentasDiarias.builder()
                        .fecha(LocalDate.of(2026, 11, 16))
                        .totalVentas(new BigDecimal("100000.00"))
                        .totalAnulado(new BigDecimal("20000.00"))
                        .cantidadOperaciones(2)
                        .build()
        );

        // Act
        ResumenVentasUsd resultado = ResumenVentasUsd.calcularDesdeResumenes(
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 16),
                resumenes,
                trm
        );
        ResumenVentasUsd resultadoVacio = ResumenVentasUsd.calcularDesdeResumenes(
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 16),
                null,
                trm
        );

        // Assert
        assertThat(trm.fuente()).isEqualTo(TasaCambio.FUENTE_API_LIVE);
        assertThat(resultadoVacio.getTotalNetoUsd()).isEqualByComparingTo("0.00");
        assertThat(resultado.getTotalVendidoCop()).isEqualByComparingTo("300000.00");
        assertThat(resultado.getTotalAnuladoCop()).isEqualByComparingTo("60000.00");
        assertThat(resultado.getTotalNetoCop()).isEqualByComparingTo("240000.00");
        assertThat(resultado.getTotalVendidoUsd()).isEqualByComparingTo("75.00");
        assertThat(resultado.getTotalAnuladoUsd()).isEqualByComparingTo("15.00");
        assertThat(resultado.getTotalNetoUsd()).isEqualByComparingTo("60.00");
        assertThat(resultado.getCantidadOperaciones()).isEqualTo(6);

        assertThatThrownBy(() -> new TasaCambio(BigDecimal.ZERO, LocalDate.now(), "CACHE"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> trm.convertirAUsd(new BigDecimal("-1.00")))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ResumenVentasUsd.calcularDesdeResumenes(null, LocalDate.now(), List.of(), trm))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ResumenVentasUsd.calcularDesdeResumenes(LocalDate.now(), LocalDate.now().minusDays(1), List.of(), trm))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ResumenVentasUsd.calcularDesdeResumenes(LocalDate.now(), LocalDate.now(), List.of(), null))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("CalendarioLaboral y ReporteEstacionalidad deben clasificar días hábiles vs festivos/domingos")
    void calendarioLaboralYEstacionalidad_CalculoCorrecto() {
        // Arrange
        CalendarioFestivosGateway gatewayStub = anio -> List.of(
                new DiaFestivo(LocalDate.of(anio, 11, 2), "Día de Todos los Santos", DiaFestivo.FUENTE_CACHE),
                new DiaFestivo(LocalDate.of(anio, 11, 16), "Independencia de Cartagena", DiaFestivo.FUENTE_CACHE)
        );
        CalendarioLaboral calendario = new CalendarioLaboral(gatewayStub);
        CalendarioLaboral calendarioVacio = new CalendarioLaboral(anio -> null);

        List<ResumenVentasDiarias> resumenesNov = List.of(
                ResumenVentasDiarias.builder()
                        .fecha(LocalDate.of(2026, 11, 16))
                        .totalVentas(new BigDecimal("120000.00"))
                        .totalAnulado(BigDecimal.ZERO)
                        .cantidadOperaciones(6)
                        .build(),
                ResumenVentasDiarias.builder()
                        .fecha(LocalDate.of(2026, 11, 17))
                        .totalVentas(new BigDecimal("240000.00"))
                        .totalAnulado(BigDecimal.ZERO)
                        .cantidadOperaciones(12)
                        .build()
        );

        // Act
        boolean lunesFestivoEsHabil = calendario.esDiaHabil(LocalDate.of(2026, 11, 16));
        boolean domingoEsHabil = calendario.esDiaHabil(LocalDate.of(2026, 11, 15));
        boolean martesSinFestivos = calendarioVacio.esDiaHabil(LocalDate.of(2026, 11, 17));
        LocalDate siguienteHabil = calendario.siguienteDiaHabil(LocalDate.of(2026, 11, 14));
        int diasHabilesSemana = calendario.contarDiasHabiles(
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 21)
        );
        ReporteEstacionalidad estacionalidad = ReporteEstacionalidad.calcular(
                2026, 11, resumenesNov, gatewayStub.obtenerFestivos(2026)
        );
        ReporteEstacionalidad estacionalidadSinDatos = ReporteEstacionalidad.calcular(
                2026, 11, null, null
        );

        // Assert
        assertThat(lunesFestivoEsHabil).isFalse();
        assertThat(domingoEsHabil).isFalse();
        assertThat(martesSinFestivos).isTrue();
        assertThat(siguienteHabil).isEqualTo(LocalDate.of(2026, 11, 17));
        assertThat(diasHabilesSemana).isEqualTo(5);
        assertThat(estacionalidad.getDiasHabiles() + estacionalidad.getDiasNoHabiles()).isEqualTo(30);
        assertThat(estacionalidad.getVentasDiasHabiles()).isEqualByComparingTo("240000.00");
        assertThat(estacionalidad.getVentasDiasNoHabiles()).isEqualByComparingTo("120000.00");
        assertThat(estacionalidadSinDatos.getFuenteFestivos()).isEqualTo(DiaFestivo.FUENTE_API_LIVE);

        assertThatThrownBy(() -> calendario.esDiaHabil(null)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> calendario.siguienteDiaHabil(null)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> calendario.contarDiasHabiles(null, LocalDate.now())).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> calendario.contarDiasHabiles(LocalDate.now(), LocalDate.now().minusDays(1)))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ReporteEstacionalidad.calcular(2026, 0, List.of(), List.of()))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ReporteEstacionalidad.calcular(1999, 11, List.of(), List.of()))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
