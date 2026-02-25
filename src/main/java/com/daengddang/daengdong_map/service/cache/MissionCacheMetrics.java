package com.daengddang.daengdong_map.service.cache;

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
public class MissionCacheMetrics {

    private static final long LOG_EVERY = 100L;

    private final LongAdder hit = new LongAdder();
    private final LongAdder miss = new LongAdder();
    private final LongAdder bypassDisabled = new LongAdder();
    private final LongAdder fallbackError = new LongAdder();
    private final LongAdder dbLoad = new LongAdder();
    private final LongAdder readError = new LongAdder();
    private final LongAdder writeError = new LongAdder();

    private final Counter hitCounter;
    private final Counter missCounter;
    private final Counter bypassDisabledCounter;
    private final Counter fallbackErrorCounter;
    private final Counter dbLoadCounter;
    private final Counter readErrorCounter;
    private final Counter writeErrorCounter;

    public MissionCacheMetrics(ObjectProvider<MeterRegistry> registryProvider) {
        MeterRegistry registry = registryProvider.getIfAvailable();
        if (registry == null) {
            hitCounter = null;
            missCounter = null;
            bypassDisabledCounter = null;
            fallbackErrorCounter = null;
            dbLoadCounter = null;
            readErrorCounter = null;
            writeErrorCounter = null;
            return;
        }

        hitCounter = Counter.builder("cache.mission.hit").description("Mission cache hit count").register(registry);
        missCounter = Counter.builder("cache.mission.miss").description("Mission cache miss count").register(registry);
        bypassDisabledCounter = Counter.builder("cache.mission.bypass.disabled")
                .description("Mission cache bypass count when cache is disabled")
                .register(registry);
        fallbackErrorCounter = Counter.builder("cache.mission.fallback.error")
                .description("Mission cache fallback-to-db count due to cache error")
                .register(registry);
        dbLoadCounter = Counter.builder("cache.mission.db.load")
                .description("Mission DB load count when cache miss/fallback occurs")
                .register(registry);
        readErrorCounter = Counter.builder("cache.mission.read.error")
                .description("Mission cache read error count")
                .register(registry);
        writeErrorCounter = Counter.builder("cache.mission.write.error")
                .description("Mission cache write error count")
                .register(registry);
    }

    public void recordHit() {
        hit.increment();
        increment(hitCounter);
        maybeLog();
    }

    public void recordMiss() {
        miss.increment();
        increment(missCounter);
        maybeLog();
    }

    public void recordBypassDisabled() {
        bypassDisabled.increment();
        increment(bypassDisabledCounter);
        maybeLog();
    }

    public void recordFallbackError() {
        fallbackError.increment();
        increment(fallbackErrorCounter);
        maybeLog();
    }

    public void recordDbLoad() {
        dbLoad.increment();
        increment(dbLoadCounter);
        maybeLog();
    }

    public void recordReadError() {
        readError.increment();
        increment(readErrorCounter);
        maybeLog();
    }

    public void recordWriteError() {
        writeError.increment();
        increment(writeErrorCounter);
        maybeLog();
    }

    private void maybeLog() {
        long total = hit.sum() + miss.sum() + bypassDisabled.sum() + fallbackError.sum() + dbLoad.sum();
        if (total > 0 && total % LOG_EVERY == 0) {
            long hitCount = hit.sum();
            long missCount = miss.sum();
            double hitRatio = (hitCount + missCount) == 0
                    ? 0.0
                    : (hitCount * 100.0) / (hitCount + missCount);
            log.info(
                    "[MissionCacheMetrics] hit={}, miss={}, hitRatio={}%, bypassDisabled={}, fallbackError={}, dbLoad={}, readError={}, writeError={}",
                    hitCount,
                    missCount,
                    String.format("%.2f", hitRatio),
                    bypassDisabled.sum(),
                    fallbackError.sum(),
                    dbLoad.sum(),
                    readError.sum(),
                    writeError.sum()
            );
        }
    }

    private void increment(Counter counter) {
        if (counter != null) {
            counter.increment();
        }
    }
}
