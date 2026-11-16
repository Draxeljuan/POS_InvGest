package com.invengest.gateway.application;

import com.invengest.gateway.domain.model.gateway.TokenValidatorGateway;
import com.invengest.gateway.domain.usecase.EvaluarAccesoGatewayUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public EvaluarAccesoGatewayUseCase evaluarAccesoGatewayUseCase(TokenValidatorGateway tokenValidatorGateway) {
        return new EvaluarAccesoGatewayUseCase(tokenValidatorGateway);
    }
}
