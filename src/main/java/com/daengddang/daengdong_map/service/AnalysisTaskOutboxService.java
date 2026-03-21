package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.analysis.AnalysisTaskMessage;
import com.daengddang.daengdong_map.domain.task.AnalysisTaskOutbox;
import com.daengddang.daengdong_map.domain.task.AnalysisTaskOutboxEventType;
import com.daengddang.daengdong_map.domain.task.AnalysisTaskOutboxStatus;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.repository.AnalysisTaskOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisTaskOutboxService {

    private final AnalysisTaskOutboxRepository analysisTaskOutboxRepository;
    private final AnalysisTaskOutboxMetrics analysisTaskOutboxMetrics;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveTaskCreatedOutbox(ExternalAnalysisTask task) {
        AnalysisTaskMessage message = AnalysisTaskMessage.from(task);

        try {
            analysisTaskOutboxRepository.save(
                    AnalysisTaskOutbox.builder()
                            .taskId(task.getTaskId())
                            .eventType(AnalysisTaskOutboxEventType.ANALYSIS_TASK_CREATED)
                            .payload(objectMapper.writeValueAsString(message))
                            .status(AnalysisTaskOutboxStatus.PENDING)
                            .attemptCount(0)
                            .build()
            );
            analysisTaskOutboxMetrics.recordSaveSuccess();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize analysis task outbox payload", e);
        }
    }
}
