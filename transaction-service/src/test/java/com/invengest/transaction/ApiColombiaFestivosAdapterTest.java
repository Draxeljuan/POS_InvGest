package com.invengest.transaction;

import com.invengest.transaction.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.transaction.domain.model.DiaFestivo;
import com.invengest.transaction.infrastructure.driver_adapters.external_api.ApiColombiaFestivosAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ApiColombiaFestivosAdapterTest {

    private static final String BASE_URL = "https://api-colombia.com/api/v1/Holiday/year";

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
    void obtenerFestivosConsultaApiLiveYRespondeDesdeCacheDentroDeTtl24Horas() {
        // Arrange
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));

        mockServer.expect(requestTo(BASE_URL + "/2026"))
                .andRespond(withSuccess(
                        "[{\"date\":\"2026-01-01T00:00:00\",\"name\":\"Año Nuevo\"},{\"date\":\"2026-11-16T00:00:00\",\"name\":\"Independencia de Cartagena\"}]",
                        MediaType.APPLICATION_JSON
                ));

        ApiColombiaFestivosAdapter adapter = new ApiColombiaFestivosAdapter(
                builder.build(), BASE_URL + "/", 86400L, false, clock);

        // Act
        adapter.precargarAlIniciar();
        List<DiaFestivo> primeraLlamada = adapter.obtenerFestivos(2026);
        clock.advance(Duration.ofHours(12));
        List<DiaFestivo> segundaLlamadaCaliente = adapter.obtenerFestivos(2026);

        // Assert
        mockServer.verify();
        assertEquals(2, primeraLlamada.size());
        assertEquals(LocalDate.of(2026, 1, 1), primeraLlamada.get(0).getFecha());
        assertEquals(DiaFestivo.FUENTE_API_LIVE, primeraLlamada.get(0).getFuente());
        assertEquals(DiaFestivo.FUENTE_CACHE, segundaLlamadaCaliente.get(0).getFuente());
    }

    @Test
    void obtenerFestivosRetornaCacheStaleCuandoExpiraTtlYApiColombiaFalla() {
        // Arrange
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));

        mockServer.expect(requestTo(BASE_URL + "/2026"))
                .andRespond(withSuccess(
                        "[{\"date\":\"2026-12-25\",\"name\":\"Navidad\"}]",
                        MediaType.APPLICATION_JSON
                ));
        mockServer.expect(requestTo(BASE_URL + "/2026"))
                .andRespond(withServerError());

        ApiColombiaFestivosAdapter adapter = new ApiColombiaFestivosAdapter(
                builder.build(), BASE_URL, 86400L, false, clock);

        // Act
        adapter.ejecutarPrecargaSegura();
        clock.advance(Duration.ofHours(25));
        List<DiaFestivo> fallbackStale = adapter.obtenerFestivos(2026);

        // Assert
        mockServer.verify();
        assertEquals(1, fallbackStale.size());
        assertEquals(DiaFestivo.FUENTE_CACHE_STALE, fallbackStale.get(0).getFuente());
        assertEquals(LocalDate.of(2026, 12, 25), fallbackStale.get(0).getFecha());
    }

    @Test
    void obtenerFestivosLanza503CuandoFallaApiORespuestaEsVaciaSinCachePrevia() {
        // Arrange
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));

        mockServer.expect(requestTo(BASE_URL + "/2027"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        mockServer.expect(ExpectedCount.once(), requestTo(BASE_URL + "/2026"))
                .andRespond(withServerError());

        ApiColombiaFestivosAdapter adapter = new ApiColombiaFestivosAdapter(
                builder.build(), BASE_URL, 86400L, false, clock);

        // Act & Assert
        assertThrows(ServicioExternoNoDisponibleException.class, () -> adapter.obtenerFestivos(2027));
        adapter.ejecutarPrecargaSegura();
        mockServer.verify();
    }
}
