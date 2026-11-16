package com.invengest.catalog.infrastructure.entry_points.security;

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
                        .requestMatchers("/internal/**").hasAuthority(InternalApiKeyFilter.AUTHORITY_INTERNAL_SERVICE)
                        .requestMatchers(HttpMethod.GET, "/api/catalog/productos/*/mejor-proveedor").hasAuthority(ROL_ADMINISTRADOR)
                        .requestMatchers(HttpMethod.GET, "/api/catalog/**").hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        .requestMatchers(HttpMethod.PUT, "/api/catalog/productos/*/descontar-stock").hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        .requestMatchers(HttpMethod.POST, "/api/catalog/**").hasAuthority(ROL_ADMINISTRADOR)
                        .requestMatchers(HttpMethod.PUT, "/api/catalog/**").hasAuthority(ROL_ADMINISTRADOR)
                        .requestMatchers(HttpMethod.DELETE, "/api/catalog/**").hasAuthority(ROL_ADMINISTRADOR)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
