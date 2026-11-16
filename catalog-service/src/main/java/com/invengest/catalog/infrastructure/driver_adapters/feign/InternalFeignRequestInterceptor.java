package com.invengest.catalog.infrastructure.driver_adapters.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InternalFeignRequestInterceptor implements RequestInterceptor {

    public static final String HEADER_INTERNAL_API_KEY = "X-Internal-Api-Key";

    private final String internalApiKey;

    public InternalFeignRequestInterceptor(@Value("${internal.api-key}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (internalApiKey != null && !internalApiKey.isBlank()) {
            template.header(HEADER_INTERNAL_API_KEY, internalApiKey);
        }
    }
}
