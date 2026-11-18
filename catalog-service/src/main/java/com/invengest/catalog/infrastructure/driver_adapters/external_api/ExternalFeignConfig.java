package com.invengest.catalog.infrastructure.driver_adapters.external_api;

import feign.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class ExternalFeignConfig {

    @Bean
    public Request.Options externalFeignRequestOptions(
            @Value("${external.api.connect-timeout-ms:1500}") long connectTimeoutMs,
            @Value("${external.api.read-timeout-ms:2500}") long readTimeoutMs) {
        return new Request.Options(
                connectTimeoutMs, TimeUnit.MILLISECONDS,
                readTimeoutMs, TimeUnit.MILLISECONDS,
                true
        );
    }
}
