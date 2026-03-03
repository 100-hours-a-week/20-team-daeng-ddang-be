package com.daengddang.daengdong_map.sse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.LongAdder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class AnalysisTaskSseRedisMetrics {

    private static final long LOG_EVERY = 100L;

    private final LongAdder dedupeDrop = new LongAdder();
    private final Counter dedupeDropCounter;

    public AnalysisTaskSseRedisMetrics(ObjectProvider<MeterRegistry> registryProvider) {
        MeterRegistry registry = registryProvider.getIfAvailable();
        if (registry == null) {
            dedupeDropCounter = null;
            return;
        }

        dedupeDropCounter = Counter.builder("analysis.sse.redis.dedupe.drop")
                .description("Dropped duplicated SSE Redis events by dedupe")
                .register(registry);
    }

    public void recordDedupeDrop() {
        dedupeDrop.increment();
        if (dedupeDropCounter != null) {
            dedupeDropCounter.increment();
        }
        long dropCount = dedupeDrop.sum();
        if (dropCount > 0 && dropCount % LOG_EVERY == 0) {
            log.info("분석 작업 SSE Redis dedupe 드롭 누적 건수={}", dropCount);
        }
    }
}
