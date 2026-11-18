package com.invengest.catalog.infrastructure.driver_adapters.external_api;

import com.invengest.catalog.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.catalog.domain.model.TasaCambio;
import com.invengest.catalog.infrastructure.driver_adapters.external_api.dto.DolarApiTrmResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DolarApiTasaCambioAdapterTest {

    @Mock
    private DolarApiFeignClient dolarApiFeignClient;

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
    @DisplayName("Consulta API_LIVE y responde desde CACHE dentro del TTL")
    void obtenerTrmVigenteConsultaApiLiveYRespondeDesdeCacheDentroDeTtl() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));
        DolarApiTrmResponseDTO dto = new DolarApiTrmResponseDTO(
                "COP", "TRM", new BigDecimal("3301.91"), "2026-11-16T00:00:00.000Z"
        );
        when(dolarApiFeignClient.obtenerTrmActual()).thenReturn(dto);

        DolarApiTasaCambioAdapter adapter = new DolarApiTasaCambioAdapter(
                dolarApiFeignClient, 3600L, false, clock
        );

        // Act
        adapter.precargarAlIniciar();
        TasaCambio primeraLlamada = adapter.obtenerTrmVigente();
        clock.advance(Duration.ofMinutes(30));
        TasaCambio segundaLlamadaEnCaliente = adapter.obtenerTrmVigente();

        // Assert
        verify(dolarApiFeignClient, times(1)).obtenerTrmActual();
        assertEquals(new BigDecimal("3301.91"), primeraLlamada.valorCopPorUsd());
        assertEquals(LocalDate.of(2026, 11, 16), primeraLlamada.fechaVigencia());
        assertEquals(TasaCambio.FUENTE_API_LIVE, primeraLlamada.fuente());
        assertEquals(TasaCambio.FUENTE_CACHE, segundaLlamadaEnCaliente.fuente());
    }

    @Test
    @DisplayName("Retorna CACHE_STALE cuando la caché expira y la API externa falla, y tolera fechas inválidas")
    void obtenerTrmVigenteRetornaCacheStaleCuandoCacheExpiraYApiExternaFalla() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));
        DolarApiTrmResponseDTO dtoConFechaInvalida = new DolarApiTrmResponseDTO(
                "COP", "TRM", new BigDecimal("4150.50"), "fecha-invalida"
        );
        when(dolarApiFeignClient.obtenerTrmActual())
                .thenReturn(dtoConFechaInvalida)
                .thenThrow(new RuntimeException("Timeout DolarAPI"));

        DolarApiTasaCambioAdapter adapter = new DolarApiTasaCambioAdapter(
                dolarApiFeignClient, 3600L, true, clock
        );

        // Act
        adapter.ejecutarPrecargaSegura();
        clock.advance(Duration.ofHours(2));
        TasaCambio fallbackStale = adapter.obtenerTrmVigente();

        // Assert
        verify(dolarApiFeignClient, times(2)).obtenerTrmActual();
        assertEquals(TasaCambio.FUENTE_CACHE_STALE, fallbackStale.fuente());
        assertEquals(new BigDecimal("4150.50"), fallbackStale.valorCopPorUsd());
        assertEquals(LocalDate.of(2026, 11, 16), fallbackStale.fechaVigencia());
    }

    @Test
    @DisplayName("Lanza ServicioExternoNoDisponibleException cuando falla API o respuesta es inválida sin caché previa")
    void obtenerTrmVigenteLanza503CuandoFallaApiORespuestaEsInvalidaSinCachePrevia() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));
        DolarApiTrmResponseDTO dtoValorCero = new DolarApiTrmResponseDTO(
                "COP", "TRM", BigDecimal.ZERO, " "
        );
        when(dolarApiFeignClient.obtenerTrmActual())
                .thenReturn(null)
                .thenReturn(dtoValorCero)
                .thenThrow(new RuntimeException("500 Internal Server Error"));

        DolarApiTasaCambioAdapter adapter = new DolarApiTasaCambioAdapter(
                dolarApiFeignClient, 3600L, false
        );
        DolarApiTasaCambioAdapter adapterConClock = new DolarApiTasaCambioAdapter(
                dolarApiFeignClient, 3600L, false, clock
        );

        // Act & Assert
        assertThrows(ServicioExternoNoDisponibleException.class, adapter::obtenerTrmVigente);
        assertThrows(ServicioExternoNoDisponibleException.class, adapterConClock::obtenerTrmVigente);
        assertDoesNotThrow(adapterConClock::ejecutarPrecargaSegura);
    }
}
