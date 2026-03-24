package com.daengddang.daengdong_map.config;

import com.daengddang.daengdong_map.analysis.AnalysisRabbitMqProperties;
import com.daengddang.daengdong_map.sse.AsyncSseProperties;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisStartupLogger {

    private final Environment environment;
    private final AnalysisRabbitMqProperties analysisRabbitMqProperties;
    private final AsyncSseProperties asyncSseProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupConfiguration() {
        boolean outboxEnabled = environment.getProperty("analysis.outbox.enabled", Boolean.class, false);

        log.info(
                "analysis startup config. profiles={}, rabbitmqEnabled={}, outboxEnabled={}, sseEnabled={}, queue={}, retryQueue={}, deadLetterQueue={}",
                Arrays.toString(environment.getActiveProfiles()),
                analysisRabbitMqProperties.isEnabled(),
                outboxEnabled,
                asyncSseProperties.isEnabled(),
                analysisRabbitMqProperties.getQueue(),
                analysisRabbitMqProperties.getRetryQueue(),
                analysisRabbitMqProperties.getDeadLetterQueue()
        );
    }
}
