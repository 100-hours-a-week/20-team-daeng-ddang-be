package com.daengddang.daengdong_map.analysis;

import com.daengddang.daengdong_map.common.exception.AnalysisBackpressureException;
import com.daengddang.daengdong_map.domain.task.AnalysisTaskOutboxStatus;
import com.daengddang.daengdong_map.repository.AnalysisTaskOutboxRepository;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisBackpressureGuard {

    private static final long OUTBOX_PENDING_CACHE_TTL_NANOS = Duration.ofMillis(200).toNanos();

    private final AnalysisBackpressureProperties properties;
    private final AnalysisRabbitMqProperties rabbitMqProperties;
    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;
    private final AnalysisTaskOutboxRepository analysisTaskOutboxRepository;

    private volatile long cachedPendingOutboxTasks;
    private volatile long cachedPendingOutboxTasksLoadedAt;

    public void validateOrThrow() {
        if (!properties.isEnabled()) {
            return;
        }

        if (rabbitMqProperties.isEnabled()) {
            validateOutboxPendingOrThrow();
            return;
        }

        long activeTasks = externalAnalysisTaskRepository.countActiveTasks();
        if (activeTasks >= properties.getMaxActiveTasks()) {
            throw new AnalysisBackpressureException(
                    properties.getRetryAfterSeconds(),
                    activeTasks,
                    properties.getMaxActiveTasks()
            );
        }
    }

    private void validateOutboxPendingOrThrow() {
        long pendingOutboxTasks = getPendingOutboxTasks();
        if (pendingOutboxTasks >= properties.getMaxOutboxPendingTasks()) {
            throw new AnalysisBackpressureException(
                    properties.getRetryAfterSeconds(),
                    pendingOutboxTasks,
                    properties.getMaxOutboxPendingTasks()
            );
        }
    }

    private long getPendingOutboxTasks() {
        long now = System.nanoTime();
        long loadedAt = cachedPendingOutboxTasksLoadedAt;
        if (now - loadedAt < OUTBOX_PENDING_CACHE_TTL_NANOS) {
            return cachedPendingOutboxTasks;
        }

        long latestCount = analysisTaskOutboxRepository.countByStatus(AnalysisTaskOutboxStatus.PENDING);
        cachedPendingOutboxTasks = latestCount;
        cachedPendingOutboxTasksLoadedAt = now;
        return latestCount;
    }
}
