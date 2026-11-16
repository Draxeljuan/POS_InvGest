package com.invengest.gateway.domain.model;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

public final class PolizaSeguridadGateway {

    public static final String HEADER_INTERNAL_API_KEY = "X-Internal-Api-Key";
    public static final String PREFIJO_HEADER_USER = "x-user-";

    private static final Set<String> RUTAS_POST_PUBLICAS = Set.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    private PolizaSeguridadGateway() {
    }

    public static String normalizarRuta(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return "/";
        }
        String colapsada = rawPath.trim().replaceAll("/{2,}", "/");
        if (!colapsada.startsWith("/")) {
            colapsada = "/" + colapsada;
        }
        try {
            String normalizada = URI.create(colapsada).normalize().getPath();
            if (normalizada == null || normalizada.isBlank()) {
                return "/";
            }
            if (normalizada.length() > 1 && normalizada.endsWith("/")) {
                normalizada = normalizada.substring(0, normalizada.length() - 1);
            }
            return normalizada;
        } catch (IllegalArgumentException ex) {
            return colapsada;
        }
    }

    public static boolean esRutaInternaBloqueada(String rawPath) {
        if (rawPath != null && (rawPath.contains("..") || rawPath.toLowerCase(Locale.ROOT).contains("%2e"))) {
            return true;
        }
        String path = normalizarRuta(rawPath).toLowerCase(Locale.ROOT);
        return path.equals("/internal")
                || path.startsWith("/internal/")
                || path.contains("/internal/")
                || path.endsWith("/internal");
    }

    public static boolean esRutaPublica(String metodoHttp, String rawPath) {
        String metodo = metodoHttp != null ? metodoHttp.trim().toUpperCase(Locale.ROOT) : "";
        if ("OPTIONS".equals(metodo)) {
            return true;
        }
        String path = normalizarRuta(rawPath);
        if ("GET".equals(metodo) && ("/actuator/health".equals(path) || path.startsWith("/actuator/health/"))) {
            return true;
        }
        return "POST".equals(metodo) && RUTAS_POST_PUBLICAS.contains(path);
    }

    public static boolean esHeaderSensibleSanitizable(String headerName) {
        if (headerName == null || headerName.isBlank()) {
            return false;
        }
        String lower = headerName.trim().toLowerCase(Locale.ROOT);
        return lower.startsWith(PREFIJO_HEADER_USER)
                || HEADER_INTERNAL_API_KEY.equalsIgnoreCase(lower);
    }
}
