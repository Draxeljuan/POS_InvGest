package com.invengest.notification.infrastructure.driver_adapters.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncMailConfig {

    @Bean(name = "notificationMailExecutor")
    public Executor notificationMailExecutor(
            @Value("${notification.async.core-pool-size:2}") int corePoolSize,
            @Value("${notification.async.max-pool-size:4}") int maxPoolSize,
            @Value("${notification.async.queue-capacity:50}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("mail-async-");
        executor.initialize();
        return executor;
    }
}
