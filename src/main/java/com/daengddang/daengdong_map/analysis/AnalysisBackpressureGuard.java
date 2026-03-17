package com.daengddang.daengdong_map.analysis;

import com.daengddang.daengdong_map.common.exception.AnalysisBackpressureException;
import com.daengddang.daengdong_map.domain.task.AnalysisTaskOutboxStatus;
import com.daengddang.daengdong_map.repository.AnalysisTaskOutboxRepository;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisBackpressureGuard {

    private final AnalysisBackpressureProperties properties;
    private final AnalysisRabbitMqProperties rabbitMqProperties;
    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;
    private final AnalysisTaskOutboxRepository analysisTaskOutboxRepository;

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
        long pendingOutboxTasks = analysisTaskOutboxRepository.countByStatus(AnalysisTaskOutboxStatus.PENDING);
        if (pendingOutboxTasks >= properties.getMaxOutboxPendingTasks()) {
            throw new AnalysisBackpressureException(
                    properties.getRetryAfterSeconds(),
                    pendingOutboxTasks,
                    properties.getMaxOutboxPendingTasks()
            );
        }
    }
}
