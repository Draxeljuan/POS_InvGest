package com.invengest.analytics.infrastructure.driver_adapters.external_api;

import com.invengest.analytics.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.analytics.domain.model.DiaFestivo;
import com.invengest.analytics.infrastructure.driver_adapters.external_api.dto.ApiColombiaHolidayDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiColombiaFestivosAdapterTest {

    @Mock
    private ApiColombiaFestivosFeignClient festivosFeignClient;

    @Test
    @DisplayName("Debe obtener festivos desde API_LIVE, cachear por año y degradar a CACHE_STALE ante fallo")
    void obtenerFestivos_CacheYFallback() {
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

        ApiColombiaFestivosAdapter adapter = new ApiColombiaFestivosAdapter(
                festivosFeignClient, 86400L, false, mutableClock
        );
        ApiColombiaFestivosAdapter adapterPreloadTrue = new ApiColombiaFestivosAdapter(
                festivosFeignClient, 86400L, true
        );

        when(festivosFeignClient.obtenerFestivosPorAnio(2026))
                .thenReturn(List.of(
                        new ApiColombiaHolidayDTO("2026-11-16T00:00:00", "Independencia de Cartagena", "Nacional"),
                        new ApiColombiaHolidayDTO("2026-12-08", " ", "Nacional"),
                        new ApiColombiaHolidayDTO("2026-12-25", null, "Nacional"),
                        new ApiColombiaHolidayDTO("invalida", "Ignorado", "Nacional"),
                        new ApiColombiaHolidayDTO("1234567890-invalida", "IgnoradoLargo", "Nacional"),
                        new ApiColombiaHolidayDTO("", "Vacio", "Nacional"),
                        new ApiColombiaHolidayDTO(null, "Nulo", "Nacional")
                ))
                .thenThrow(new RuntimeException("Fallo red"));

        // Act
        adapter.precargarAlIniciar();
        List<DiaFestivo> live = adapter.obtenerFestivos(2026);
        nowRef.set(nowRef.get().plus(Duration.ofHours(1)));
        List<DiaFestivo> cached = adapter.obtenerFestivos(2026);
        nowRef.set(nowRef.get().plus(Duration.ofHours(25)));
        List<DiaFestivo> stale = adapter.obtenerFestivos(2026);
        adapter.ejecutarPrecargaSegura();
        adapterPreloadTrue.precargarAlIniciar();

        // Assert
        assertThat(live).hasSize(3);
        assertThat(live.getFirst().getFuente()).isEqualTo(DiaFestivo.FUENTE_API_LIVE);
        assertThat(live.get(1).getNombre()).isEqualTo("Festivo Nacional");
        assertThat(cached.getFirst().getFuente()).isEqualTo(DiaFestivo.FUENTE_CACHE);
        assertThat(stale.getFirst().getFuente()).isEqualTo(DiaFestivo.FUENTE_CACHE_STALE);
    }

    @Test
    @DisplayName("Debe lanzar ServicioExternoNoDisponibleException cuando un año sin caché devuelve vacío, nulo o fechas inválidas")
    void obtenerFestivos_SinCacheRespuestasInvalidas_LanzaExcepcion() {
        // Arrange
        ApiColombiaFestivosAdapter adapter = new ApiColombiaFestivosAdapter(
                festivosFeignClient, 86400L, false, Clock.systemUTC()
        );
        when(festivosFeignClient.obtenerFestivosPorAnio(2027)).thenReturn(List.of());
        when(festivosFeignClient.obtenerFestivosPorAnio(2028)).thenReturn(null);
        when(festivosFeignClient.obtenerFestivosPorAnio(2029)).thenReturn(List.of(
                new ApiColombiaHolidayDTO("invalida", "Invalido", "Nacional")
        ));
        when(festivosFeignClient.obtenerFestivosPorAnio(2026)).thenThrow(new RuntimeException("Timeout"));

        // Act & Assert
        adapter.ejecutarPrecargaSegura();
        assertThatThrownBy(() -> adapter.obtenerFestivos(2027))
                .isInstanceOf(ServicioExternoNoDisponibleException.class);
        assertThatThrownBy(() -> adapter.obtenerFestivos(2028))
                .isInstanceOf(ServicioExternoNoDisponibleException.class);
        assertThatThrownBy(() -> adapter.obtenerFestivos(2029))
                .isInstanceOf(ServicioExternoNoDisponibleException.class);
    }
}
