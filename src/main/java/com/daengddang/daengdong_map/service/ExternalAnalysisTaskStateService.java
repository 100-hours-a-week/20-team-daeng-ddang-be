package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskStatus;
import com.daengddang.daengdong_map.event.AnalysisTaskStatusChangedEvent;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalAnalysisTaskStateService {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 1000;

    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markRunningIfPending(String taskId) {
        int updated = externalAnalysisTaskRepository.markRunningIfPending(
                taskId,
                ExternalAnalysisTaskStatus.PENDING,
                ExternalAnalysisTaskStatus.RUNNING,
                LocalDateTime.now()
        );
        boolean changed = updated > 0;
        if (changed) {
            eventPublisher.publishEvent(new AnalysisTaskStatusChangedEvent(taskId));
        }
        return changed;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccessIfRunning(String taskId, String resultType, String resultId) {
        int updated = externalAnalysisTaskRepository.markSuccessIfRunning(
                taskId,
                ExternalAnalysisTaskStatus.RUNNING,
                ExternalAnalysisTaskStatus.SUCCESS,
                LocalDateTime.now(),
                resultType,
                resultId
        );
        if (updated > 0) {
            eventPublisher.publishEvent(new AnalysisTaskStatusChangedEvent(taskId));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFail(String taskId, String errorCode, String errorMessage) {
        int updated = externalAnalysisTaskRepository.markFail(
                taskId,
                List.of(ExternalAnalysisTaskStatus.PENDING, ExternalAnalysisTaskStatus.RUNNING),
                ExternalAnalysisTaskStatus.FAIL,
                LocalDateTime.now(),
                errorCode,
                sanitizeErrorMessage(errorMessage)
        );
        if (updated > 0) {
            eventPublisher.publishEvent(new AnalysisTaskStatusChangedEvent(taskId));
        }
    }

    private String sanitizeErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        if (errorMessage.length() <= ERROR_MESSAGE_MAX_LENGTH) {
            return errorMessage;
        }
        return errorMessage.substring(0, ERROR_MESSAGE_MAX_LENGTH);
    }
}
