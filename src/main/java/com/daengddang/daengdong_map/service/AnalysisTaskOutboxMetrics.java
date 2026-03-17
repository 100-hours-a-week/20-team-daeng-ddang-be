package com.daengddang.daengdong_map.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AnalysisTaskOutboxMetrics {

    private final Counter saveSuccessCounter;
    private final Counter publishSuccessCounter;
    private final Counter publishFailCounter;
    private final Timer publishDurationTimer;
    private final AtomicInteger pendingGaugeValue = new AtomicInteger();
    private final AtomicInteger failedGaugeValue = new AtomicInteger();

    public AnalysisTaskOutboxMetrics(ObjectProvider<MeterRegistry> registryProvider) {
        MeterRegistry registry = registryProvider.getIfAvailable();
        if (registry == null) {
            saveSuccessCounter = null;
            publishSuccessCounter = null;
            publishFailCounter = null;
            publishDurationTimer = null;
            return;
        }

        saveSuccessCounter = Counter.builder("analysis.outbox.save.success")
                .description("Analysis task outbox save success count")
                .register(registry);
        publishSuccessCounter = Counter.builder("analysis.outbox.publish.success")
                .description("Analysis task outbox publish success count")
                .register(registry);
        publishFailCounter = Counter.builder("analysis.outbox.publish.fail")
                .description("Analysis task outbox publish fail count")
                .register(registry);
        publishDurationTimer = Timer.builder("analysis.outbox.publish.duration")
                .description("Analysis task outbox publish duration")
                .register(registry);

        Gauge.builder("analysis.outbox.pending.count", pendingGaugeValue, AtomicInteger::get)
                .description("Analysis task outbox pending row count")
                .register(registry);
        Gauge.builder("analysis.outbox.failed.count", failedGaugeValue, AtomicInteger::get)
                .description("Analysis task outbox failed row count")
                .register(registry);
    }

    public void recordSaveSuccess() {
        increment(saveSuccessCounter);
    }

    public void recordPublishSuccess(Duration duration) {
        increment(publishSuccessCounter);
        recordDuration(duration);
    }

    public void recordPublishFail(Duration duration) {
        increment(publishFailCounter);
        recordDuration(duration);
    }

    public void updatePendingCount(int count) {
        pendingGaugeValue.set(Math.max(count, 0));
    }

    public void updateFailedCount(int count) {
        failedGaugeValue.set(Math.max(count, 0));
    }

    private void recordDuration(Duration duration) {
        if (publishDurationTimer != null && duration != null && !duration.isNegative()) {
            publishDurationTimer.record(duration);
        }
    }

    private void increment(Counter counter) {
        if (counter != null) {
            counter.increment();
        }
    }
}
