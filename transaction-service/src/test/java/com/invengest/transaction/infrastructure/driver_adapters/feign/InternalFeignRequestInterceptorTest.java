package com.invengest.transaction.infrastructure.driver_adapters.feign;

import com.invengest.transaction.infrastructure.driver_adapters.external_api.ApiColombiaFestivosFeignClient;
import feign.RequestTemplate;
import feign.Target;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalFeignRequestInterceptorTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Debe inyectar X-Internal-Api-Key y Authorization para microservicios internos")
    void debeInyectarHeadersEnLlamadasInternas() {
        // Arrange
        InternalFeignRequestInterceptor interceptor = new InternalFeignRequestInterceptor("secret-internal-key");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-token-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        RequestTemplate templateInterno = new RequestTemplate();
        templateInterno.feignTarget(new Target.HardCodedTarget<>(CatalogFeignClient.class, "catalog-service", "http://localhost:8082"));

        // Act
        interceptor.apply(templateInterno);

        // Assert
        assertTrue(templateInterno.headers().get(InternalFeignRequestInterceptor.HEADER_INTERNAL_API_KEY).contains("secret-internal-key"));
        assertTrue(templateInterno.headers().get(HttpHeaders.AUTHORIZATION).contains("Bearer jwt-token-123"));
    }

    @Test
    @DisplayName("No debe inyectar headers cuando el target Feign termina en -api como festivos-api")
    void noDebeInyectarHeadersCuandoTargetTerminaEnApi() {
        // Arrange
        InternalFeignRequestInterceptor interceptor = new InternalFeignRequestInterceptor("secret-internal-key");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-token-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        RequestTemplate templateExterno = new RequestTemplate();
        templateExterno.feignTarget(new Target.HardCodedTarget<>(
                ApiColombiaFestivosFeignClient.class, "festivos-api", "https://api-colombia.com/api/v1/Holiday/year"));

        // Act
        interceptor.apply(templateExterno);
        RequestContextHolder.resetRequestAttributes();
        new InternalFeignRequestInterceptor(" ").apply(new RequestTemplate());

        // Assert
        assertFalse(templateExterno.headers().containsKey(InternalFeignRequestInterceptor.HEADER_INTERNAL_API_KEY));
        assertFalse(templateExterno.headers().containsKey(HttpHeaders.AUTHORIZATION));
    }
}
