package com.invengest.auth.infrastructure.driver_adapters.feign;

import feign.RequestTemplate;
import feign.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalFeignRequestInterceptorTest {

    @Test
    @DisplayName("Debe adjuntar X-Internal-Api-Key solo en clientes internos con llave no vacía")
    void apply_CuandoClienteInternoOExterno_DebeAislarCabecera() {
        // Arrange
        InternalFeignRequestInterceptor interceptor = new InternalFeignRequestInterceptor("secret-api-key");
        InternalFeignRequestInterceptor blankInterceptor = new InternalFeignRequestInterceptor("   ");
        RequestTemplate internalTemplate = new RequestTemplate();
        RequestTemplate blankTemplate = new RequestTemplate();
        RequestTemplate externalTemplate = new RequestTemplate();
        externalTemplate.feignTarget(new Target.HardCodedTarget<>(Object.class, "dolar-api", "https://co.dolarapi.com/v1/trm"));

        // Act
        interceptor.apply(internalTemplate);
        blankInterceptor.apply(blankTemplate);
        interceptor.apply(externalTemplate);

        // Assert
        assertTrue(internalTemplate.headers().containsKey(InternalFeignRequestInterceptor.HEADER_INTERNAL_API_KEY));
        assertTrue(blankTemplate.headers().isEmpty());
        assertTrue(externalTemplate.headers().isEmpty());
    }
}
