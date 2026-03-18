package com.daengddang.daengdong_map.analysis;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.atomic.LongAdder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class AnalysisTaskRabbitMetrics {

    private static final long LOG_EVERY = 100L;

    private final LongAdder publishSuccess = new LongAdder();
    private final LongAdder publishFail = new LongAdder();
    private final LongAdder consumeSuccess = new LongAdder();
    private final LongAdder consumeFail = new LongAdder();

    private final Counter publishSuccessCounter;
    private final Counter publishFailCounter;
    private final Counter consumeSuccessCounter;
    private final Counter consumeFailCounter;
    private final Timer consumeDurationTimer;

    public AnalysisTaskRabbitMetrics(ObjectProvider<MeterRegistry> registryProvider) {
        MeterRegistry registry = registryProvider.getIfAvailable();
        if (registry == null) {
            publishSuccessCounter = null;
            publishFailCounter = null;
            consumeSuccessCounter = null;
            consumeFailCounter = null;
            consumeDurationTimer = null;
            return;
        }

        publishSuccessCounter = Counter.builder("analysis.rabbitmq.publish.success")
                .description("Analysis task RabbitMQ publish success count")
                .register(registry);
        publishFailCounter = Counter.builder("analysis.rabbitmq.publish.fail")
                .description("Analysis task RabbitMQ publish fail count")
                .register(registry);
        consumeSuccessCounter = Counter.builder("analysis.rabbitmq.consume.success")
                .description("Analysis task RabbitMQ consume success count")
                .register(registry);
        consumeFailCounter = Counter.builder("analysis.rabbitmq.consume.fail")
                .description("Analysis task RabbitMQ consume fail count")
                .register(registry);
        consumeDurationTimer = Timer.builder("analysis.rabbitmq.consume.duration")
                .description("Analysis task RabbitMQ consume duration")
                .register(registry);
    }

    public void recordPublishSuccess() {
        publishSuccess.increment();
        increment(publishSuccessCounter);
        maybeLog();
    }

    public void recordPublishFail() {
        publishFail.increment();
        increment(publishFailCounter);
        maybeLog();
    }

    public void recordConsumeSuccess(Duration duration) {
        consumeSuccess.increment();
        increment(consumeSuccessCounter);
        recordDuration(duration);
        maybeLog();
    }

    public void recordConsumeFail(Duration duration) {
        consumeFail.increment();
        increment(consumeFailCounter);
        recordDuration(duration);
        maybeLog();
    }

    private void recordDuration(Duration duration) {
        if (consumeDurationTimer != null && duration != null && !duration.isNegative()) {
            consumeDurationTimer.record(duration);
        }
    }

    private void increment(Counter counter) {
        if (counter != null) {
            counter.increment();
        }
    }

    private void maybeLog() {
        long total = publishSuccess.sum() + publishFail.sum() + consumeSuccess.sum() + consumeFail.sum();
        if (total > 0 && total % LOG_EVERY == 0) {
            log.info("[AnalysisTaskRabbitMetrics] publishSuccess={}, publishFail={}, consumeSuccess={}, consumeFail={}",
                    publishSuccess.sum(), publishFail.sum(), consumeSuccess.sum(), consumeFail.sum());
        }
    }
}
