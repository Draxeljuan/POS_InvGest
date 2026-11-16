package com.invengest.analytics.infrastructure.entry_points.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class InternalApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_INTERNAL_API_KEY = "X-Internal-Api-Key";
    public static final String AUTHORITY_INTERNAL_SERVICE = "ROLE_SERVICE";

    private final String expectedApiKey;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;

    public InternalApiKeyFilter(
            String expectedApiKey,
            JsonAuthenticationEntryPoint authenticationEntryPoint) {
        this.expectedApiKey = expectedApiKey;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path != null && (path.startsWith("/internal/") || path.startsWith("/api/analytics/internal/"))) {
            String providedKey = request.getHeader(HEADER_INTERNAL_API_KEY);
            if (!isValidKey(providedKey)) {
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.writeUnauthorized(response, "X-Internal-Api-Key inválida o ausente");
                return;
            }

            UsernamePasswordAuthenticationToken serviceAuth = new UsernamePasswordAuthenticationToken(
                    "internal-service",
                    null,
                    List.of(new SimpleGrantedAuthority(AUTHORITY_INTERNAL_SERVICE))
            );
            serviceAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(serviceAuth);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidKey(String providedKey) {
        if (providedKey == null || providedKey.isBlank() || expectedApiKey == null || expectedApiKey.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                providedKey.getBytes(StandardCharsets.UTF_8),
                expectedApiKey.getBytes(StandardCharsets.UTF_8)
        );
    }
}
