package com.invengest.catalog;

import com.invengest.catalog.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.catalog.domain.model.TasaCambio;
import com.invengest.catalog.infrastructure.driver_adapters.external_api.DolarApiTasaCambioAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DolarApiTasaCambioAdapterTest {

    private static final String DOLAR_API_URL = "https://co.dolarapi.com/v1/trm";

    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> current;

        MutableClock(Instant initial) {
            this.current = new AtomicReference<>(initial);
        }

        void advance(Duration duration) {
            current.updateAndGet(inst -> inst.plus(duration));
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current.get();
        }
    }

    @Test
    void obtenerTrmVigenteConsultaApiLiveYRespondeDesdeCacheDentroDeTtl() {
        // Arrange
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));

        mockServer.expect(requestTo(DOLAR_API_URL))
                .andRespond(withSuccess(
                        "{\"unidad\":\"COP\",\"nombre\":\"TRM\",\"valor\":3301.91,\"fechaActualizacion\":\"2026-11-16T00:00:00.000Z\"}",
                        MediaType.APPLICATION_JSON
                ));

        DolarApiTasaCambioAdapter adapter = new DolarApiTasaCambioAdapter(
                builder.build(), DOLAR_API_URL, 3600L, false, clock);

        // Act
        adapter.precargarAlIniciar();
        TasaCambio primeraLlamada = adapter.obtenerTrmVigente();
        clock.advance(Duration.ofMinutes(30));
        TasaCambio segundaLlamadaEnCaliente = adapter.obtenerTrmVigente();

        // Assert
        mockServer.verify();
        assertEquals(new BigDecimal("3301.91"), primeraLlamada.valorCopPorUsd());
        assertEquals(LocalDate.of(2026, 11, 16), primeraLlamada.fechaVigencia());
        assertEquals(TasaCambio.FUENTE_API_LIVE, primeraLlamada.fuente());
        assertEquals(TasaCambio.FUENTE_CACHE, segundaLlamadaEnCaliente.fuente());
    }

    @Test
    void obtenerTrmVigenteRetornaCacheStaleCuandoCacheExpiraYApiExternaFalla() {
        // Arrange
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));

        mockServer.expect(requestTo(DOLAR_API_URL))
                .andRespond(withSuccess(
                        "{\"unidad\":\"COP\",\"nombre\":\"TRM\",\"valor\":4150.50,\"fechaActualizacion\":\"fecha-invalida\"}",
                        MediaType.APPLICATION_JSON
                ));
        mockServer.expect(requestTo(DOLAR_API_URL))
                .andRespond(withServerError());

        DolarApiTasaCambioAdapter adapter = new DolarApiTasaCambioAdapter(
                builder.build(), DOLAR_API_URL, 3600L, false, clock);

        // Act
        adapter.ejecutarPrecargaSegura();
        clock.advance(Duration.ofHours(2));
        TasaCambio fallbackStale = adapter.obtenerTrmVigente();

        // Assert
        mockServer.verify();
        assertEquals(TasaCambio.FUENTE_CACHE_STALE, fallbackStale.fuente());
        assertEquals(new BigDecimal("4150.50"), fallbackStale.valorCopPorUsd());
    }

    @Test
    void obtenerTrmVigenteLanza503CuandoFallaApiORespuestaEsInvalidaSinCachePrevia() {
        // Arrange
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));

        mockServer.expect(requestTo(DOLAR_API_URL))
                .andRespond(withSuccess("{\"valor\":0,\"fechaActualizacion\":null}", MediaType.APPLICATION_JSON));
        mockServer.expect(ExpectedCount.once(), requestTo(DOLAR_API_URL))
                .andRespond(withServerError());

        DolarApiTasaCambioAdapter adapter = new DolarApiTasaCambioAdapter(
                builder.build(), DOLAR_API_URL, 3600L, false, clock);

        // Act & Assert
        assertThrows(ServicioExternoNoDisponibleException.class, adapter::obtenerTrmVigente);
        adapter.ejecutarPrecargaSegura();
        mockServer.verify();
    }
}
