package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskStatus;
import com.daengddang.daengdong_map.event.AnalysisTaskStatusChangedEvent;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
            log.info("외부 분석 작업 상태 전이 성공. taskId={}, from={}, to={}",
                    taskId, ExternalAnalysisTaskStatus.PENDING, ExternalAnalysisTaskStatus.RUNNING);
            eventPublisher.publishEvent(new AnalysisTaskStatusChangedEvent(taskId));
        } else {
            ExternalAnalysisTaskStatus currentStatus = externalAnalysisTaskRepository.findStatusByTaskId(taskId)
                    .orElse(null);
            log.warn("외부 분석 작업 상태 전이 실패. taskId={}, expectedStatus={}, targetStatus={}, currentStatus={}",
                    taskId, ExternalAnalysisTaskStatus.PENDING, ExternalAnalysisTaskStatus.RUNNING, currentStatus);
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
    public void markPendingForRetryIfRunning(String taskId, String errorCode, String errorMessage) {
        int updated = externalAnalysisTaskRepository.markPendingForRetryIfRunning(
                taskId,
                ExternalAnalysisTaskStatus.RUNNING,
                ExternalAnalysisTaskStatus.PENDING,
                errorCode,
                sanitizeErrorMessage(errorMessage)
        );
        if (updated > 0) {
            log.info("외부 분석 작업 재시도 상태 전이 성공. taskId={}, from={}, to={}, errorCode={}",
                    taskId, ExternalAnalysisTaskStatus.RUNNING, ExternalAnalysisTaskStatus.PENDING, errorCode);
            eventPublisher.publishEvent(new AnalysisTaskStatusChangedEvent(taskId));
        } else {
            ExternalAnalysisTaskStatus currentStatus = externalAnalysisTaskRepository.findStatusByTaskId(taskId)
                    .orElse(null);
            log.warn("외부 분석 작업 재시도 상태 전이 실패. taskId={}, expectedStatus={}, targetStatus={}, currentStatus={}, errorCode={}",
                    taskId, ExternalAnalysisTaskStatus.RUNNING, ExternalAnalysisTaskStatus.PENDING, currentStatus, errorCode);
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
            log.info("외부 분석 작업 실패 상태 전이 성공. taskId={}, to={}, errorCode={}",
                    taskId, ExternalAnalysisTaskStatus.FAIL, errorCode);
            eventPublisher.publishEvent(new AnalysisTaskStatusChangedEvent(taskId));
        } else {
            ExternalAnalysisTaskStatus currentStatus = externalAnalysisTaskRepository.findStatusByTaskId(taskId)
                    .orElse(null);
            log.warn("외부 분석 작업 실패 상태 전이 실패. taskId={}, targetStatus={}, currentStatus={}, errorCode={}",
                    taskId, ExternalAnalysisTaskStatus.FAIL, currentStatus, errorCode);
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
