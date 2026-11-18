package com.invengest.analytics.infrastructure.driver_adapters.external_api;

import com.invengest.analytics.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.analytics.domain.model.TasaCambio;
import com.invengest.analytics.infrastructure.driver_adapters.external_api.dto.DolarApiTrmResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DolarApiTasaCambioAdapterTest {

    @Mock
    private DolarApiFeignClient dolarApiFeignClient;

    @Test
    @DisplayName("Debe obtener TRM desde API_LIVE, servir desde CACHE dentro del TTL y degradar a CACHE_STALE ante fallo")
    void obtenerTrmVigente_CacheYFallback() {
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

        DolarApiTasaCambioAdapter adapter = new DolarApiTasaCambioAdapter(
                dolarApiFeignClient, 3600L, false, mutableClock
        );
        DolarApiTasaCambioAdapter adapterPreloadTrue = new DolarApiTasaCambioAdapter(
                dolarApiFeignClient, 3600L, true
        );

        when(dolarApiFeignClient.obtenerTrmActual())
                .thenReturn(new DolarApiTrmResponseDTO("COP", "TRM", new BigDecimal("3301.91"), "2026-11-16T05:00:00Z"))
                .thenThrow(new RuntimeException("Fallo red"));

        // Act
        adapter.precargarAlIniciar();
        TasaCambio live = adapter.obtenerTrmVigente();
        nowRef.set(nowRef.get().plus(Duration.ofMinutes(30)));
        TasaCambio cached = adapter.obtenerTrmVigente();
        nowRef.set(nowRef.get().plus(Duration.ofHours(2)));
        TasaCambio stale = adapter.obtenerTrmVigente();
        adapter.ejecutarPrecargaSegura();
        adapterPreloadTrue.precargarAlIniciar();

        // Assert
        assertThat(live.fuente()).isEqualTo(TasaCambio.FUENTE_API_LIVE);
        assertThat(live.valorCopPorUsd()).isEqualByComparingTo("3301.91");
        assertThat(cached.fuente()).isEqualTo(TasaCambio.FUENTE_CACHE);
        assertThat(stale.fuente()).isEqualTo(TasaCambio.FUENTE_CACHE_STALE);
        assertThat(stale.valorCopPorUsd()).isEqualByComparingTo("3301.91");
    }

    @Test
    @DisplayName("Debe lanzar ServicioExternoNoDisponibleException cuando falla en frío sin caché o la respuesta es inválida")
    void obtenerTrmVigente_FalloSinCacheYRespuestasInvalidas() {
        // Arrange
        DolarApiTasaCambioAdapter adapter = new DolarApiTasaCambioAdapter(
                dolarApiFeignClient, 3600L, false, Clock.systemUTC()
        );
        when(dolarApiFeignClient.obtenerTrmActual())
                .thenReturn(new DolarApiTrmResponseDTO("COP", "TRM", BigDecimal.ZERO, null))
                .thenReturn(null)
                .thenReturn(new DolarApiTrmResponseDTO("COP", "TRM", null, " "))
                .thenThrow(new RuntimeException("Timeout"))
                .thenReturn(new DolarApiTrmResponseDTO("COP", "TRM", new BigDecimal("4000.00"), "fecha-invalida"));

        // Act & Assert
        assertThatThrownBy(adapter::obtenerTrmVigente)
                .isInstanceOf(ServicioExternoNoDisponibleException.class);
        assertThatThrownBy(adapter::obtenerTrmVigente)
                .isInstanceOf(ServicioExternoNoDisponibleException.class);
        assertThatThrownBy(adapter::obtenerTrmVigente)
                .isInstanceOf(ServicioExternoNoDisponibleException.class);
        adapter.ejecutarPrecargaSegura();

        TasaCambio conFechaInvalida = adapter.obtenerTrmVigente();
        assertThat(conFechaInvalida.valorCopPorUsd()).isEqualByComparingTo("4000.00");
    }
}
