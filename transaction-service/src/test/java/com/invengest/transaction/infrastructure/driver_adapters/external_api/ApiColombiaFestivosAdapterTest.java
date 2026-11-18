package com.invengest.transaction.infrastructure.driver_adapters.external_api;

import com.invengest.transaction.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.transaction.domain.model.DiaFestivo;
import com.invengest.transaction.infrastructure.driver_adapters.external_api.dto.ApiColombiaHolidayDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiColombiaFestivosAdapterTest {

    @Mock
    private ApiColombiaFestivosFeignClient festivosFeignClient;

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
    @DisplayName("Debe obtener festivos desde API_LIVE y servir desde CACHE dentro del TTL")
    void debeObtenerFestivosDesdeApiLiveYCacheCaliente() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));
        when(festivosFeignClient.obtenerFestivosPorAnio(2026)).thenReturn(List.of(
                new ApiColombiaHolidayDTO("2026-01-01T00:00:00", "Año Nuevo"),
                new ApiColombiaHolidayDTO("2026-11-16", "   "),
                new ApiColombiaHolidayDTO(null, "Ignorado"),
                new ApiColombiaHolidayDTO("fecha-invalida", "Ignorado")
        ));

        ApiColombiaFestivosAdapter adapter = new ApiColombiaFestivosAdapter(
                festivosFeignClient, 86400L, false, clock);

        // Act
        adapter.precargarAlIniciar();
        List<DiaFestivo> primeraLlamada = adapter.obtenerFestivos(2026);
        clock.advance(Duration.ofHours(12));
        List<DiaFestivo> segundaLlamadaCaliente = adapter.obtenerFestivos(2026);

        // Assert
        verify(festivosFeignClient, times(1)).obtenerFestivosPorAnio(2026);
        assertEquals(2, primeraLlamada.size());
        assertEquals(LocalDate.of(2026, 1, 1), primeraLlamada.get(0).getFecha());
        assertEquals("Año Nuevo", primeraLlamada.get(0).getNombre());
        assertEquals("Festivo Nacional", primeraLlamada.get(1).getNombre());
        assertEquals(DiaFestivo.FUENTE_API_LIVE, primeraLlamada.get(0).getFuente());
        assertEquals(DiaFestivo.FUENTE_CACHE, segundaLlamadaCaliente.get(0).getFuente());
    }

    @Test
    @DisplayName("Debe retornar CACHE_STALE cuando expira el TTL y el cliente Feign falla")
    void debeRetornarCacheStaleCuandoExpiraTtlYFallaApi() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));
        when(festivosFeignClient.obtenerFestivosPorAnio(2026))
                .thenReturn(List.of(new ApiColombiaHolidayDTO("2026-12-25", "Navidad")))
                .thenThrow(new RuntimeException("Timeout 503"));

        ApiColombiaFestivosAdapter adapter = new ApiColombiaFestivosAdapter(
                festivosFeignClient, 86400L, false, clock);

        // Act
        adapter.ejecutarPrecargaSegura();
        clock.advance(Duration.ofHours(25));
        List<DiaFestivo> fallbackStale = adapter.obtenerFestivos(2026);

        // Assert
        verify(festivosFeignClient, times(2)).obtenerFestivosPorAnio(2026);
        assertEquals(1, fallbackStale.size());
        assertEquals(DiaFestivo.FUENTE_CACHE_STALE, fallbackStale.get(0).getFuente());
        assertEquals(LocalDate.of(2026, 12, 25), fallbackStale.get(0).getFecha());
    }

    @Test
    @DisplayName("Debe lanzar ServicioExternoNoDisponibleException ante fallo o respuesta vacía/inválida sin caché")
    void debeLanzarExcepcionAnteFalloORespuestaVaciaSinCache() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2026-11-16T12:00:00Z"));
        when(festivosFeignClient.obtenerFestivosPorAnio(2027)).thenReturn(List.of());
        when(festivosFeignClient.obtenerFestivosPorAnio(2028)).thenReturn(List.of(new ApiColombiaHolidayDTO("mal", "X")));
        when(festivosFeignClient.obtenerFestivosPorAnio(2026)).thenThrow(new RuntimeException("Error red"));

        ApiColombiaFestivosAdapter adapter = new ApiColombiaFestivosAdapter(
                festivosFeignClient, 86400L, true, clock);
        ApiColombiaFestivosAdapter adapterDefault = new ApiColombiaFestivosAdapter(
                festivosFeignClient, 86400L, false);

        // Act & Assert
        assertThrows(ServicioExternoNoDisponibleException.class, () -> adapter.obtenerFestivos(2027));
        assertThrows(ServicioExternoNoDisponibleException.class, () -> adapter.obtenerFestivos(2028));
        assertDoesNotThrow(adapter::ejecutarPrecargaSegura);
        assertDoesNotThrow(adapter::precargarAlIniciar);
        assertDoesNotThrow(adapterDefault::precargarAlIniciar);
    }
}
