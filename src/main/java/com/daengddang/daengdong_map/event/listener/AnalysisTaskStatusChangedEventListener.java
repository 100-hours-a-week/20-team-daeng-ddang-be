package com.daengddang.daengdong_map.event.listener;

import com.daengddang.daengdong_map.dto.response.task.AnalysisTaskDetailResponse;
import com.daengddang.daengdong_map.event.AnalysisTaskStatusChangedEvent;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import com.daengddang.daengdong_map.sse.AnalysisTaskSseRedisPublisher;
import com.daengddang.daengdong_map.sse.AnalysisTaskSseService;
import com.daengddang.daengdong_map.sse.AsyncSseRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisTaskStatusChangedEventListener {

    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;
    private final AnalysisTaskSseService analysisTaskSseService;
    private final AnalysisTaskSseRedisPublisher analysisTaskSseRedisPublisher;
    private final AsyncSseRedisProperties asyncSseRedisProperties;

    @Async("asyncSseExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(AnalysisTaskStatusChangedEvent event) {
        externalAnalysisTaskRepository.findWithContextByTaskId(event.getTaskId())
                .map(AnalysisTaskDetailResponse::from)
                .ifPresent(analysisTaskSseService::publishStatus);

        if (asyncSseRedisProperties.isEnabled()) {
            analysisTaskSseRedisPublisher.publish(event.getTaskId());
        }
    }
}
