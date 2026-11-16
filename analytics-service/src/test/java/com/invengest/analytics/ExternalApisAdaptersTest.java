package com.invengest.analytics;

import com.invengest.analytics.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.analytics.domain.model.DiaFestivo;
import com.invengest.analytics.domain.model.TasaCambio;
import com.invengest.analytics.infrastructure.driver_adapters.external_api.ApiColombiaFestivosAdapter;
import com.invengest.analytics.infrastructure.driver_adapters.external_api.DolarApiTasaCambioAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExternalApisAdaptersTest {

    private static final String DOLAR_URL = "https://co.dolarapi.com/v1/trm";
    private static final String FESTIVOS_BASE_URL = "https://api-colombia.com/api/v1/Holiday/year";

    @Test
    @DisplayName("DolarApiTasaCambioAdapter debe usar caché dentro del TTL y hacer fallback a CACHE_STALE cuando falla la red")
    void dolarApiAdapter_CacheYFallback() {
        // Arrange
        AtomicReference<Instant> nowRef = new AtomicReference<>(Instant.parse("2026-11-16T10:00:00Z"));
        Clock mutableClock = new Clock() {
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
                return nowRef.get();
            }
        };

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DolarApiTasaCambioAdapter adapter = new DolarApiTasaCambioAdapter(
                builder.build(), DOLAR_URL, 3600L, false, mutableClock
        );

        server.expect(requestTo(DOLAR_URL))
                .andRespond(withSuccess(
                        "{\"unidad\":\"COP\",\"nombre\":\"TRM\",\"valor\":3301.91,\"fechaActualizacion\":\"fecha-invalida\"}",
                        MediaType.APPLICATION_JSON
                ));
        server.expect(requestTo(DOLAR_URL)).andRespond(withServerError());

        // Act
        adapter.precargarAlIniciar();
        adapter.ejecutarPrecargaSegura();
        TasaCambio live = adapter.obtenerTrmVigente();
        nowRef.set(nowRef.get().plus(Duration.ofMinutes(30)));
        TasaCambio cached = adapter.obtenerTrmVigente();
        nowRef.set(nowRef.get().plus(Duration.ofHours(2)));
        TasaCambio stale = adapter.obtenerTrmVigente();

        // Assert
        assertThat(live.fuente()).isEqualTo(TasaCambio.FUENTE_CACHE);
        assertThat(live.valorCopPorUsd()).isEqualByComparingTo("3301.91");
        assertThat(cached.fuente()).isEqualTo(TasaCambio.FUENTE_CACHE);
        assertThat(stale.fuente()).isEqualTo(TasaCambio.FUENTE_CACHE_STALE);
        assertThat(stale.valorCopPorUsd()).isEqualByComparingTo("3301.91");
        server.verify();
    }

    @Test
    @DisplayName("DolarApiTasaCambioAdapter debe lanzar ServicioExternoNoDisponibleException si falla en frío sin caché")
    void dolarApiAdapter_FalloSinCache() {
        // Arrange
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DolarApiTasaCambioAdapter adapter = new DolarApiTasaCambioAdapter(
                builder.build(), DOLAR_URL, 3600L, false, Clock.systemUTC()
        );
        server.expect(ExpectedCount.once(), requestTo(DOLAR_URL))
                .andRespond(withSuccess("{\"valor\":0,\"fechaActualizacion\":null}", MediaType.APPLICATION_JSON));
        server.expect(ExpectedCount.once(), requestTo(DOLAR_URL))
                .andRespond(withServerError());

        // Act & Assert
        assertThatThrownBy(adapter::obtenerTrmVigente)
                .isInstanceOf(ServicioExternoNoDisponibleException.class);
        adapter.ejecutarPrecargaSegura();
        server.verify();
    }

    @Test
    @DisplayName("ApiColombiaFestivosAdapter debe cachear por año, parsear distintos formatos de fecha y degradar a CACHE_STALE")
    void festivosAdapter_CacheYFallback() {
        // Arrange
        AtomicReference<Instant> nowRef = new AtomicReference<>(Instant.parse("2026-11-16T10:00:00Z"));
        Clock mutableClock = new Clock() {
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
                return nowRef.get();
            }
        };

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ApiColombiaFestivosAdapter adapter = new ApiColombiaFestivosAdapter(
                builder.build(), FESTIVOS_BASE_URL + "/", 86400L, false, mutableClock
        );

        server.expect(requestTo(FESTIVOS_BASE_URL + "/2026"))
                .andRespond(withSuccess(
                        "[{\"date\":\"2026-11-16T00:00:00\",\"name\":\"Independencia de Cartagena\"},"
                                + "{\"date\":\"2026-12-08\",\"name\":\" \"},"
                                + "{\"date\":\"invalida\",\"name\":\"Ignorado\"},"
                                + "{\"date\":\"\",\"name\":\"Vacio\"}]",
                        MediaType.APPLICATION_JSON
                ));
        server.expect(requestTo(FESTIVOS_BASE_URL + "/2026")).andRespond(withServerError());
        server.expect(requestTo(FESTIVOS_BASE_URL + "/2027")).andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(FESTIVOS_BASE_URL + "/2028"))
                .andRespond(withSuccess("[{\"date\":\"invalida\",\"name\":\"Invalido\"}]", MediaType.APPLICATION_JSON));

        // Act
        adapter.precargarAlIniciar();
        adapter.ejecutarPrecargaSegura();
        List<DiaFestivo> cached = adapter.obtenerFestivos(2026);
        nowRef.set(nowRef.get().plus(Duration.ofHours(25)));
        List<DiaFestivo> stale = adapter.obtenerFestivos(2026);

        // Assert
        assertThat(cached).hasSize(2);
        assertThat(cached.get(1).getNombre()).isEqualTo("Festivo Nacional");
        assertThat(cached.getFirst().getFuente()).isEqualTo(DiaFestivo.FUENTE_CACHE);
        assertThat(stale).hasSize(2);
        assertThat(stale.getFirst().getFuente()).isEqualTo(DiaFestivo.FUENTE_CACHE_STALE);

        // Act & Assert (Año sin caché previa que retorna vacío o inválido -> 503)
        assertThatThrownBy(() -> adapter.obtenerFestivos(2027))
                .isInstanceOf(ServicioExternoNoDisponibleException.class);
        assertThatThrownBy(() -> adapter.obtenerFestivos(2028))
                .isInstanceOf(ServicioExternoNoDisponibleException.class);
        server.verify();
    }
}
