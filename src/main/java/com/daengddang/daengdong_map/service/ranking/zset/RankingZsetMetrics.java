package com.daengddang.daengdong_map.service.ranking.zset;

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
public class RankingZsetMetrics {

    private static final long LOG_EVERY = 100L;

    private final LongAdder buildSuccess = new LongAdder();
    private final LongAdder buildError = new LongAdder();
    private final LongAdder readSuccess = new LongAdder();
    private final LongAdder readError = new LongAdder();
    private final LongAdder shadowMismatch = new LongAdder();
    private final LongAdder updateSuccess = new LongAdder();
    private final LongAdder updateError = new LongAdder();

    private final Counter buildSuccessCounter;
    private final Counter buildErrorCounter;
    private final Counter readSuccessCounter;
    private final Counter readErrorCounter;
    private final Counter shadowMismatchCounter;
    private final Counter updateSuccessCounter;
    private final Counter updateErrorCounter;
    private final Timer buildDurationTimer;

    public RankingZsetMetrics(ObjectProvider<MeterRegistry> registryProvider) {
        MeterRegistry registry = registryProvider.getIfAvailable();
        if (registry == null) {
            buildSuccessCounter = null;
            buildErrorCounter = null;
            readSuccessCounter = null;
            readErrorCounter = null;
            shadowMismatchCounter = null;
            updateSuccessCounter = null;
            updateErrorCounter = null;
            buildDurationTimer = null;
            return;
        }

        buildSuccessCounter = Counter.builder("ranking.zset.build.success")
                .description("Ranking zset build success count")
                .register(registry);
        buildErrorCounter = Counter.builder("ranking.zset.build.error")
                .description("Ranking zset build error count")
                .register(registry);
        readSuccessCounter = Counter.builder("ranking.zset.read.success")
                .description("Ranking zset read success count")
                .register(registry);
        readErrorCounter = Counter.builder("ranking.zset.read.error")
                .description("Ranking zset read error count")
                .register(registry);
        shadowMismatchCounter = Counter.builder("ranking.zset.shadow.mismatch")
                .description("Ranking zset shadow mismatch count")
                .register(registry);
        updateSuccessCounter = Counter.builder("ranking.zset.update.success")
                .description("Ranking zset realtime update success count")
                .register(registry);
        updateErrorCounter = Counter.builder("ranking.zset.update.error")
                .description("Ranking zset realtime update error count")
                .register(registry);
        buildDurationTimer = Timer.builder("ranking.zset.build.duration")
                .description("Ranking zset build duration")
                .register(registry);
    }

    public void recordBuildSuccess(Duration duration) {
        buildSuccess.increment();
        increment(buildSuccessCounter);
        recordDuration(duration);
        maybeLog();
    }

    public void recordBuildError(Duration duration) {
        buildError.increment();
        increment(buildErrorCounter);
        recordDuration(duration);
        maybeLog();
    }

    public void recordReadSuccess() {
        readSuccess.increment();
        increment(readSuccessCounter);
        maybeLog();
    }

    public void recordReadError() {
        readError.increment();
        increment(readErrorCounter);
        maybeLog();
    }

    public void recordShadowMismatch() {
        shadowMismatch.increment();
        increment(shadowMismatchCounter);
        maybeLog();
    }

    public void recordUpdateSuccess() {
        updateSuccess.increment();
        increment(updateSuccessCounter);
        maybeLog();
    }

    public void recordUpdateError() {
        updateError.increment();
        increment(updateErrorCounter);
        maybeLog();
    }

    private void recordDuration(Duration duration) {
        if (buildDurationTimer != null && duration != null && !duration.isNegative()) {
            buildDurationTimer.record(duration);
        }
    }

    private void increment(Counter counter) {
        if (counter != null) {
            counter.increment();
        }
    }

    private void maybeLog() {
        long total = buildSuccess.sum()
                + buildError.sum()
                + readSuccess.sum()
                + readError.sum()
                + shadowMismatch.sum()
                + updateSuccess.sum()
                + updateError.sum();
        if (total > 0 && total % LOG_EVERY == 0) {
            log.info(
                    "[RankingZsetMetrics] buildSuccess={}, buildError={}, readSuccess={}, readError={}, shadowMismatch={}, updateSuccess={}, updateError={}",
                    buildSuccess.sum(),
                    buildError.sum(),
                    readSuccess.sum(),
                    readError.sum(),
                    shadowMismatch.sum(),
                    updateSuccess.sum(),
                    updateError.sum()
            );
        }
    }
}
