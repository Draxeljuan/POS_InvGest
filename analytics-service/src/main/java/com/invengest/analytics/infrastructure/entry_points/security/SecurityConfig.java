package com.invengest.analytics.infrastructure.entry_points.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final String ROL_VENDEDOR = "Vendedor";

    private final String jwtSecret;
    private final String jwtIssuer;
    private final String internalApiKey;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    private final JsonAccessDeniedHandler jsonAccessDeniedHandler;

    public SecurityConfig(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.issuer:invengest-pos-auth}") String jwtIssuer,
            @Value("${internal.api-key}") String internalApiKey,
            JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint,
            JsonAccessDeniedHandler jsonAccessDeniedHandler) {
        this.jwtSecret = jwtSecret;
        this.jwtIssuer = jwtIssuer;
        this.internalApiKey = internalApiKey;
        this.jsonAuthenticationEntryPoint = jsonAuthenticationEntryPoint;
        this.jsonAccessDeniedHandler = jsonAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        InternalApiKeyFilter internalApiKeyFilter = new InternalApiKeyFilter(internalApiKey, jsonAuthenticationEntryPoint);
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtSecret, jwtIssuer);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                        .accessDeniedHandler(jsonAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        // Proyecciones internas CQRS protegidas con X-Internal-Api-Key
                        .requestMatchers("/internal/**", "/api/analytics/internal/**")
                        .hasAuthority(InternalApiKeyFilter.AUTHORITY_INTERNAL_SERVICE)
                        // Reportes gerenciales (Excel, PDF, Resumen USD) exclusivos para Administrador
                        .requestMatchers(HttpMethod.GET, "/api/analytics/reports/**")
                        .hasAuthority(ROL_ADMINISTRADOR)
                        // Consultas operativas de Dashboard, Top Productos, Alertas y Estacionalidad
                        .requestMatchers(HttpMethod.GET,
                                "/api/analytics/dashboard",
                                "/api/analytics/dashboard/**",
                                "/api/analytics/top-products",
                                "/api/analytics/top-products/**",
                                "/api/analytics/alerts",
                                "/api/analytics/alerts/**",
                                "/api/analytics/estacionalidad",
                                "/api/analytics/estacionalidad/**"
                        ).hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public RouterFunction<ServerResponse> actuatorHealthEndpoint() {
        return RouterFunctions.route()
                .GET("/actuator/health", req -> ServerResponse.ok().body(Map.of("status", "UP", "service", "analytics-service")))
                .build();
    }
}
