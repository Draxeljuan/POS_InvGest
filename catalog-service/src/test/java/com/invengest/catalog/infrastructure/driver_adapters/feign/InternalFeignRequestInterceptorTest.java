package com.invengest.catalog.infrastructure.driver_adapters.feign;

import com.invengest.catalog.infrastructure.driver_adapters.external_api.DolarApiFeignClient;
import feign.RequestTemplate;
import feign.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalFeignRequestInterceptorTest {

    @Test
    @DisplayName("Inyecta X-Internal-Api-Key en clientes internos y omite clientes externos terminados en -api")
    void inyectaHeaderEnClientesInternosYOmiteClientesExternos() {
        // Arrange
        InternalFeignRequestInterceptor interceptor = new InternalFeignRequestInterceptor("secret-internal-key");
        InternalFeignRequestInterceptor emptyInterceptor = new InternalFeignRequestInterceptor(" ");

        RequestTemplate internalTemplate = new RequestTemplate();
        internalTemplate.feignTarget(new Target.HardCodedTarget<>(AuthFeignClient.class, "auth-service", "http://auth-service:8081"));

        RequestTemplate externalApiTemplate = new RequestTemplate();
        externalApiTemplate.feignTarget(new Target.HardCodedTarget<>(DolarApiFeignClient.class, "dolar-api", "https://co.dolarapi.com/v1/trm"));

        RequestTemplate emptyKeyTemplate = new RequestTemplate();

        // Act
        interceptor.apply(internalTemplate);
        interceptor.apply(externalApiTemplate);
        emptyInterceptor.apply(emptyKeyTemplate);

        // Assert
        assertTrue(internalTemplate.headers().containsKey(InternalFeignRequestInterceptor.HEADER_INTERNAL_API_KEY));
        assertFalse(externalApiTemplate.headers().containsKey(InternalFeignRequestInterceptor.HEADER_INTERNAL_API_KEY));
        assertFalse(emptyKeyTemplate.headers().containsKey(InternalFeignRequestInterceptor.HEADER_INTERNAL_API_KEY));
    }
}
