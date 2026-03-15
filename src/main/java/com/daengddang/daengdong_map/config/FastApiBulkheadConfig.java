package com.daengddang.daengdong_map.config;

import com.daengddang.daengdong_map.ai.FastApiProperties;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FastApiBulkheadConfig {

    @Bean
    public Bulkhead fastApiBulkhead(FastApiProperties fastApiProperties) {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(fastApiProperties.getBulkheadMaxConcurrentCalls())
                .maxWaitDuration(fastApiProperties.getBulkheadMaxWaitDuration())
                .build();
        return Bulkhead.of("fastApiBulkhead", config);
    }
}
