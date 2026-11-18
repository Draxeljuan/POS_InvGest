package com.invengest.catalog.infrastructure.driver_adapters.external_api;

import com.invengest.catalog.domain.exception.ServicioExternoNoDisponibleException;
import com.invengest.catalog.domain.model.TasaCambio;
import com.invengest.catalog.domain.model.gateway.TasaCambioGateway;
import com.invengest.catalog.infrastructure.driver_adapters.external_api.dto.DolarApiTrmResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class DolarApiTasaCambioAdapter implements TasaCambioGateway {

    private static final Logger log = LoggerFactory.getLogger(DolarApiTasaCambioAdapter.class);
    private static final ZoneId ZONA_BOGOTA = ZoneId.of("America/Bogota");

    private record EntradaCacheTrm(BigDecimal valorCopPorUsd, LocalDate fechaVigencia, Instant actualizadoEn) {}

    private final DolarApiFeignClient dolarApiFeignClient;
    private final long cacheTtlSeconds;
    private final boolean preloadEnabled;
    private final Clock clock;
    private final AtomicReference<EntradaCacheTrm> cacheTrm = new AtomicReference<>();

    @Autowired
    public DolarApiTasaCambioAdapter(
            DolarApiFeignClient dolarApiFeignClient,
            @Value("${trm.cache.ttl-seconds:3600}") long cacheTtlSeconds,
            @Value("${trm.cache.preload-enabled:true}") boolean preloadEnabled) {
        this(dolarApiFeignClient, cacheTtlSeconds, preloadEnabled, Clock.system(ZONA_BOGOTA));
    }

    public DolarApiTasaCambioAdapter(
            DolarApiFeignClient dolarApiFeignClient,
            long cacheTtlSeconds,
            boolean preloadEnabled,
            Clock clock) {
        this.dolarApiFeignClient = dolarApiFeignClient;
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
        try {
            obtenerTrmVigente();
            log.info("trm_cache_precargada cliente=dolar-api");
        } catch (RuntimeException ex) {
            log.warn("trm_cache_precarga_omitida cliente=dolar-api causa={}", ex.getMessage());
        }
    }

    @Override
    public TasaCambio obtenerTrmVigente() {
        EntradaCacheTrm actual = cacheTrm.get();
        Instant ahora = clock.instant();

        if (actual != null && ahora.isBefore(actual.actualizadoEn().plusSeconds(cacheTtlSeconds))) {
            return new TasaCambio(actual.valorCopPorUsd(), actual.fechaVigencia(), TasaCambio.FUENTE_CACHE);
        }

        try {
            DolarApiTrmResponseDTO respuesta = dolarApiFeignClient.obtenerTrmActual();

            if (respuesta == null || respuesta.valor() == null || respuesta.valor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServicioExternoNoDisponibleException("Respuesta inválida desde DolarAPI TRM.");
            }

            LocalDate fechaVigencia = parsearFechaVigencia(respuesta.fechaActualizacion());
            EntradaCacheTrm nuevaEntrada = new EntradaCacheTrm(respuesta.valor(), fechaVigencia, ahora);
            cacheTrm.set(nuevaEntrada);

            return new TasaCambio(nuevaEntrada.valorCopPorUsd(), nuevaEntrada.fechaVigencia(), TasaCambio.FUENTE_API_LIVE);
        } catch (RuntimeException ex) {
            if (actual != null) {
                log.warn("trm_api_fallo_usando_cache_stale valorPrevio={} causa={}",
                        actual.valorCopPorUsd(), ex.getMessage());
                return new TasaCambio(actual.valorCopPorUsd(), actual.fechaVigencia(), TasaCambio.FUENTE_CACHE_STALE);
            }
            log.error("trm_api_no_disponible_sin_cache causa={}", ex.getMessage());
            throw new ServicioExternoNoDisponibleException(
                    "No fue posible obtener la TRM vigente desde DolarAPI y no existe valor en caché.", ex);
        }
    }

    private LocalDate parsearFechaVigencia(String fechaActualizacion) {
        if (fechaActualizacion == null || fechaActualizacion.isBlank()) {
            return LocalDate.now(clock);
        }
        try {
            return OffsetDateTime.parse(fechaActualizacion.trim()).toLocalDate();
        } catch (DateTimeParseException ex) {
            return LocalDate.now(clock);
        }
    }
}
