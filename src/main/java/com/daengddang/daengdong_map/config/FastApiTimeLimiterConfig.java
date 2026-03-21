package com.daengddang.daengdong_map.config;

import com.daengddang.daengdong_map.ai.FastApiProperties;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class FastApiTimeLimiterConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService fastApiExecutorService(FastApiProperties fastApiProperties) {
        return new ThreadPoolExecutor(
                fastApiProperties.getExecutorCorePoolSize(),
                fastApiProperties.getExecutorMaxPoolSize(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(fastApiProperties.getExecutorQueueCapacity()),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    @Bean
    @Qualifier("missionFastApiTimeLimiter")
    public TimeLimiter missionFastApiTimeLimiter(FastApiProperties fastApiProperties) {
        return newTimeLimiter(fastApiProperties.getMissionHardTimeout());
    }

    @Bean
    @Qualifier("expressionFastApiTimeLimiter")
    public TimeLimiter expressionFastApiTimeLimiter(FastApiProperties fastApiProperties) {
        return newTimeLimiter(fastApiProperties.getExpressionHardTimeout());
    }

    @Bean
    @Qualifier("healthcareFastApiTimeLimiter")
    public TimeLimiter healthcareFastApiTimeLimiter(FastApiProperties fastApiProperties) {
        return newTimeLimiter(fastApiProperties.getHealthcareHardTimeout());
    }

    @Bean
    @Qualifier("chatFastApiTimeLimiter")
    public TimeLimiter chatFastApiTimeLimiter(FastApiProperties fastApiProperties) {
        return newTimeLimiter(fastApiProperties.getChatHardTimeout());
    }

    private TimeLimiter newTimeLimiter(java.time.Duration timeout) {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(timeout)
                .cancelRunningFuture(true)
                .build();
        return TimeLimiter.of(config);
    }
}
