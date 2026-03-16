package com.daengddang.daengdong_map.event.listener;

import com.daengddang.daengdong_map.analysis.AnalysisTaskMessage;
import com.daengddang.daengdong_map.analysis.AnalysisTaskRabbitPublisher;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.event.ExternalAnalysisTaskCreatedEvent;
import com.daengddang.daengdong_map.repository.ExternalAnalysisTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "analysis.rabbitmq", name = "enabled", havingValue = "true")
public class AnalysisTaskRabbitPublishListener {

    private final ExternalAnalysisTaskRepository externalAnalysisTaskRepository;
    private final AnalysisTaskRabbitPublisher analysisTaskRabbitPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(ExternalAnalysisTaskCreatedEvent event) {
        ExternalAnalysisTask task = externalAnalysisTaskRepository.findByTaskId(event.getTaskId())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        AnalysisTaskMessage message = AnalysisTaskMessage.from(task);
        analysisTaskRabbitPublisher.publish(message);

        log.info("분석 작업 RabbitMQ 발행 이벤트 처리 완료. taskId={}, type={}, traceId={}",
                message.taskId(), message.type(), message.traceId());
    }
}
