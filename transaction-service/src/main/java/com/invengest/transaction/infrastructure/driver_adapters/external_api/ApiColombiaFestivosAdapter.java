package com.invengest.transaction.infrastructure.driver_adapters.external_api;

import com.invengest.transaction.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.transaction.domain.model.DiaFestivo;
import com.invengest.transaction.domain.model.gateway.CalendarioFestivosGateway;
import com.invengest.transaction.infrastructure.driver_adapters.external_api.dto.ApiColombiaHolidayDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiColombiaFestivosAdapter implements CalendarioFestivosGateway {

    private static final Logger log = LoggerFactory.getLogger(ApiColombiaFestivosAdapter.class);
    private static final ParameterizedTypeReference<List<ApiColombiaHolidayDTO>> HOLIDAY_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private record EntradaCacheFestivos(List<DiaFestivo> festivosBase, Instant actualizadoEn) {}

    private final RestClient restClient;
    private final String festivosApiBaseUrl;
    private final long cacheTtlSeconds;
    private final boolean preloadEnabled;
    private final Clock clock;
    private final Map<Integer, EntradaCacheFestivos> cachePorAnio = new ConcurrentHashMap<>();

    @Autowired
    public ApiColombiaFestivosAdapter(
            RestClient festivosRestClient,
            @Value("${festivos.api.url:https://api-colombia.com/api/v1/Holiday/year}") String festivosApiBaseUrl,
            @Value("${festivos.cache.ttl-seconds:86400}") long cacheTtlSeconds,
            @Value("${festivos.cache.preload-enabled:true}") boolean preloadEnabled) {
        this(festivosRestClient, festivosApiBaseUrl, cacheTtlSeconds, preloadEnabled, Clock.systemUTC());
    }

    public ApiColombiaFestivosAdapter(
            RestClient restClient,
            String festivosApiBaseUrl,
            long cacheTtlSeconds,
            boolean preloadEnabled,
            Clock clock) {
        this.restClient = restClient;
        this.festivosApiBaseUrl = festivosApiBaseUrl != null && festivosApiBaseUrl.endsWith("/")
                ? festivosApiBaseUrl.substring(0, festivosApiBaseUrl.length() - 1)
                : festivosApiBaseUrl;
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.preloadEnabled = preloadEnabled;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void precargarAlIniciar() {
        if (!preloadEnabled) {
            return;
        }
        CompletableFuture.runAsync(this::ejecutarPrecargaSegura);
    }

    public void ejecutarPrecargaSegura() {
        int anioActual = LocalDate.now(clock).getYear();
        try {
            obtenerFestivos(anioActual);
            log.info("festivos_cache_precargada anio={} url={}/{}", anioActual, festivosApiBaseUrl, anioActual);
        } catch (RuntimeException ex) {
            log.warn("festivos_cache_precarga_omitida anio={} causa={}", anioActual, ex.getMessage());
        }
    }

    @Override
    public List<DiaFestivo> obtenerFestivos(int anio) {
        EntradaCacheFestivos actual = cachePorAnio.get(anio);
        Instant ahora = clock.instant();

        if (actual != null && ahora.isBefore(actual.actualizadoEn().plusSeconds(cacheTtlSeconds))) {
            return clonarConFuente(actual.festivosBase(), DiaFestivo.FUENTE_CACHE);
        }

        String urlAnio = festivosApiBaseUrl + "/" + anio;
        try {
            List<ApiColombiaHolidayDTO> respuesta = restClient.get()
                    .uri(urlAnio)
                    .retrieve()
                    .body(HOLIDAY_LIST_TYPE);

            if (respuesta == null || respuesta.isEmpty()) {
                throw new ServicioExternoNoDisponibleException(
                        "Respuesta vacía o inválida desde API Colombia Festivos para el año " + anio);
            }

            List<DiaFestivo> festivosParseados = new ArrayList<>();
            for (ApiColombiaHolidayDTO dto : respuesta) {
                LocalDate fecha = parsearFecha(dto.date());
                if (fecha != null) {
                    String nombre = dto.name() != null && !dto.name().isBlank() ? dto.name().trim() : "Festivo Nacional";
                    festivosParseados.add(new DiaFestivo(fecha, nombre, DiaFestivo.FUENTE_API_LIVE));
                }
            }

            if (festivosParseados.isEmpty()) {
                throw new ServicioExternoNoDisponibleException(
                        "No se encontraron fechas válidas en la respuesta de API Colombia Festivos para el año " + anio);
            }

            cachePorAnio.put(anio, new EntradaCacheFestivos(List.copyOf(festivosParseados), ahora));
            return clonarConFuente(festivosParseados, DiaFestivo.FUENTE_API_LIVE);
        } catch (RuntimeException ex) {
            if (actual != null) {
                log.warn("festivos_api_fallo_usando_cache_stale anio={} url={} causa={}",
                        anio, urlAnio, ex.getMessage());
                return clonarConFuente(actual.festivosBase(), DiaFestivo.FUENTE_CACHE_STALE);
            }
            log.error("festivos_api_no_disponible_sin_cache anio={} url={} causa={}",
                    anio, urlAnio, ex.getMessage());
            throw new ServicioExternoNoDisponibleException(
                    "No fue posible obtener los festivos de Colombia para el año " + anio + " y no existe valor en caché.",
                    ex);
        }
    }

    private List<DiaFestivo> clonarConFuente(List<DiaFestivo> base, String fuente) {
        return base.stream()
                .map(d -> new DiaFestivo(d.getFecha(), d.getNombre(), fuente))
                .toList();
    }

    private LocalDate parsearFecha(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }
        String texto = rawDate.trim();
        try {
            if (texto.length() >= 10) {
                return LocalDate.parse(texto.substring(0, 10));
            }
            return LocalDateTime.parse(texto).toLocalDate();
        } catch (DateTimeParseException ex) {
            try {
                return OffsetDateTime.parse(texto).toLocalDate();
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }
}
