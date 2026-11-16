package com.invengest.transaction.infrastructure.driver_adapters.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && !authHeader.isBlank()) {
                template.header(HttpHeaders.AUTHORIZATION, authHeader);
            }
        }
    }
}
