package com.invengest.transaction.infrastructure.entry_points.security;

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
                        // Anulación de ventas: exclusiva para Administrador
                        .requestMatchers(HttpMethod.POST, "/api/transactions/ventas/*/anular", "/api/transaction/ventas/*/anular").hasAuthority(ROL_ADMINISTRADOR)
                        // Ajustes manuales Kardex: exclusivos para Administrador
                        .requestMatchers(HttpMethod.POST, "/api/transactions/kardex/**", "/api/transaction/kardex/**").hasAuthority(ROL_ADMINISTRADOR)
                        // Ventas POS, comprobantes y consultas: Administrador y Vendedor
                        .requestMatchers(HttpMethod.POST, "/api/transactions/ventas", "/api/transaction/ventas").hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        .requestMatchers(HttpMethod.POST, "/api/transactions/ventas/*/comprobante/enviar", "/api/transaction/ventas/*/comprobante/enviar").hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        .requestMatchers(HttpMethod.GET, "/api/transactions/ventas/**", "/api/transaction/ventas/**").hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        // Caja (resumen, cierre, historial): Administrador y Vendedor
                        .requestMatchers("/api/transactions/caja/**", "/api/transaction/caja/**").hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        // Lectura Kardex, clientes, métodos de pago y catálogos auxiliares: Administrador y Vendedor
                        .requestMatchers(HttpMethod.GET, "/api/transactions/kardex/**", "/api/transaction/kardex/**").hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        .requestMatchers("/api/transactions/clientes/**", "/api/transaction/clientes/**").hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        .requestMatchers(HttpMethod.GET, "/api/transactions/metodos-pago/**", "/api/transaction/metodos-pago/**").hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        .requestMatchers(HttpMethod.GET, "/api/transactions/tipos-documento/**", "/api/transaction/tipos-documento/**").hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        .requestMatchers(HttpMethod.GET, "/api/transactions/tipos-movimiento/**", "/api/transaction/tipos-movimiento/**").hasAnyAuthority(ROL_ADMINISTRADOR, ROL_VENDEDOR)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
