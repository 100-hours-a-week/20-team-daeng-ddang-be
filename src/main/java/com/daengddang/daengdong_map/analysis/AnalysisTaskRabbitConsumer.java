package com.daengddang.daengdong_map.analysis;

import com.daengddang.daengdong_map.service.ExternalAnalysisTaskProcessor;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "analysis.rabbitmq", name = "enabled", havingValue = "true")
public class AnalysisTaskRabbitConsumer {

    private final AnalysisRabbitMqProperties properties;
    private final ExternalAnalysisTaskProcessor externalAnalysisTaskProcessor;
    private final AnalysisTaskRabbitMetrics analysisTaskRabbitMetrics;

    @RabbitListener(queues = "#{analysisTaskQueue.name}")
    public void consume(AnalysisTaskMessage message) {
        Instant startedAt = Instant.now();

        log.info("분석 작업 메시지 수신. taskId={}, type={}, traceId={}, queue={}",
                message.taskId(), message.type(), message.traceId(), properties.getQueue());

        try {
            externalAnalysisTaskProcessor.process(message.taskId());
            analysisTaskRabbitMetrics.recordConsumeSuccess(Duration.between(startedAt, Instant.now()));
            log.info("분석 작업 메시지 처리 완료. taskId={}, type={}, traceId={}",
                    message.taskId(), message.type(), message.traceId());
        } catch (RuntimeException ex) {
            Duration duration = Duration.between(startedAt, Instant.now());
            analysisTaskRabbitMetrics.recordConsumeFail(duration);
            log.error("분석 작업 메시지 처리 실패. taskId={}, type={}, traceId={}, queue={}, deadLetterQueue={}, errorType={}, errorMessage={}, durationMs={}",
                    message.taskId(),
                    message.type(),
                    message.traceId(),
                    properties.getQueue(),
                    properties.getDeadLetterQueue(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    duration.toMillis(),
                    ex);
            throw new AmqpRejectAndDontRequeueException(
                    "analysis task consumer failed and message will be dead-lettered. taskId=" + message.taskId(),
                    ex
            );
        }
    }
}
