package com.daengddang.daengdong_map.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AnalysisAsyncConfig {

    @Bean(name = "analysisExecutor")
    public ThreadPoolTaskExecutor analysisExecutor(
            @Value("${analysis.async.executor.core-pool-size:4}") int corePoolSize,
            @Value("${analysis.async.executor.max-pool-size:8}") int maxPoolSize,
            @Value("${analysis.async.executor.queue-capacity:200}") int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("analysis-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
