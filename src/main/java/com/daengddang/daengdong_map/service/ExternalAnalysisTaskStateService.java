package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskStatus;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalAnalysisTaskStateService {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 1000;

    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markRunningIfPending(String taskId) {
        int updated = externalAnalysisTaskRepository.markRunningIfPending(
                taskId,
                ExternalAnalysisTaskStatus.PENDING,
                ExternalAnalysisTaskStatus.RUNNING,
                LocalDateTime.now()
        );
        return updated > 0;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccessIfRunning(String taskId, String resultType, String resultId) {
        externalAnalysisTaskRepository.markSuccessIfRunning(
                taskId,
                ExternalAnalysisTaskStatus.RUNNING,
                ExternalAnalysisTaskStatus.SUCCESS,
                LocalDateTime.now(),
                resultType,
                resultId
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFail(String taskId, String errorCode, String errorMessage) {
        externalAnalysisTaskRepository.markFail(
                taskId,
                List.of(ExternalAnalysisTaskStatus.PENDING, ExternalAnalysisTaskStatus.RUNNING),
                ExternalAnalysisTaskStatus.FAIL,
                LocalDateTime.now(),
                errorCode,
                sanitizeErrorMessage(errorMessage)
        );
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
