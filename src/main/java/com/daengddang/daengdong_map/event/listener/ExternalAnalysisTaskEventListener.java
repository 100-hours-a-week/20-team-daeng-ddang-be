package com.daengddang.daengdong_map.event.listener;

import com.daengddang.daengdong_map.event.ExternalAnalysisTaskCreatedEvent;
import com.daengddang.daengdong_map.service.ExternalAnalysisTaskProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalAnalysisTaskEventListener {

    private final ExternalAnalysisTaskProcessor externalAnalysisTaskProcessor;

    @Async("analysisExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(ExternalAnalysisTaskCreatedEvent event) {
        log.info("외부 분석 작업 생성 이벤트 수신(커밋 이후, 비동기 처리 시작). taskId={}, type={}",
                event.getTaskId(), event.getType());
        externalAnalysisTaskProcessor.process(event.getTaskId());
    }
}
