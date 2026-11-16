package com.invengest.notification.application;

import com.invengest.notification.domain.model.gateway.CorreoGateway;
import com.invengest.notification.domain.usecase.EnviarNotificacionUseCase;
import com.invengest.notification.domain.usecase.PlantillaCorreoFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public PlantillaCorreoFactory plantillaCorreoFactory() {
        return new PlantillaCorreoFactory();
    }

    @Bean
    public EnviarNotificacionUseCase enviarNotificacionUseCase(
            CorreoGateway correoGateway,
            PlantillaCorreoFactory plantillaCorreoFactory) {
        return new EnviarNotificacionUseCase(correoGateway, plantillaCorreoFactory);
    }
}
