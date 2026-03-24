package com.daengddang.daengdong_map.analysis;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.AnalysisTaskAlreadyFailedException;
import com.daengddang.daengdong_map.common.exception.AnalysisTaskAlreadyRunningException;
import com.daengddang.daengdong_map.common.exception.AnalysisTaskAlreadySucceededException;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.common.exception.RetryableAnalysisTaskException;
import com.daengddang.daengdong_map.service.ExternalAnalysisTaskProcessor;
import com.daengddang.daengdong_map.service.ExternalAnalysisTaskStateService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
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
    private final ExternalAnalysisTaskStateService externalAnalysisTaskStateService;
    private final AnalysisTaskRabbitMetrics analysisTaskRabbitMetrics;

    @RabbitListener(queues = "#{analysisTaskQueue.name}")
    public void consume(AnalysisTaskMessage message, Message amqpMessage) {
        Instant startedAt = Instant.now();
        int retryCount = AnalysisTaskRetryMessageRecoverer.readRetryCount(amqpMessage);
        Long queueDelayMs = millisBetween(message.requestedAt(), LocalDateTime.now());
        Long brokerDelayMs = millisBetween(message.publishedAt(), LocalDateTime.now());

        log.info("분석 작업 메시지 수신. taskId={}, type={}, traceId={}, queue={}, retryCount={}, requestedAt={}, publishedAt={}, queueDelayMs={}, brokerDelayMs={}",
                message.taskId(),
                message.type(),
                message.traceId(),
                properties.getQueue(),
                retryCount,
                message.requestedAt(),
                message.publishedAt(),
                queueDelayMs,
                brokerDelayMs);

        try {
            log.info("분석 작업 메시지 처리 위임 시작. taskId={}, type={}, traceId={}, retryCount={}",
                    message.taskId(), message.type(), message.traceId(), retryCount);
            externalAnalysisTaskProcessor.processOrThrow(message.taskId());
            analysisTaskRabbitMetrics.recordConsumeSuccess(Duration.between(startedAt, Instant.now()));
            log.info("분석 작업 메시지 처리 완료. taskId={}, type={}, traceId={}",
                    message.taskId(), message.type(), message.traceId());
        } catch (BaseException ex) {
            Duration duration = Duration.between(startedAt, Instant.now());
            analysisTaskRabbitMetrics.recordConsumeFail(duration);
            if (isRetryable(ex) && retryCount < properties.getRetryMaxAttempts()) {
                log.warn("재시도 가능한 분석 작업 처리 실패를 감지했습니다. taskId={}, traceId={}, errorCode={}, retryCount={}, maxRetryAttempts={}",
                        message.taskId(),
                        message.traceId(),
                        ex.getErrorCode().name(),
                        retryCount,
                        properties.getRetryMaxAttempts(),
                        ex);
                externalAnalysisTaskStateService.markPendingForRetryIfRunning(
                        message.taskId(),
                        ex.getErrorCode().name(),
                        ex.getMessage()
                );
                log.warn("분석 작업 메시지 처리 실패. retry queue로 이동합니다. taskId={}, type={}, traceId={}, retryCount={}, maxRetryAttempts={}, retryQueue={}, errorCode={}, durationMs={}",
                        message.taskId(),
                        message.type(),
                        message.traceId(),
                        retryCount + 1,
                        properties.getRetryMaxAttempts(),
                        properties.getRetryQueue(),
                        ex.getErrorCode().name(),
                        duration.toMillis(),
                        ex);
                throw new RetryableAnalysisTaskException(
                        "재시도 가능한 분석 작업 소비 실패입니다. errorCode=" + ex.getErrorCode().name(),
                        ex
                );
            }
            log.error("재시도 불가이거나 재시도 횟수를 초과한 분석 작업 처리 실패를 감지했습니다. taskId={}, traceId={}, errorCode={}, retryCount={}, maxRetryAttempts={}",
                    message.taskId(),
                    message.traceId(),
                    ex.getErrorCode().name(),
                    retryCount,
                    properties.getRetryMaxAttempts(),
                    ex);
            externalAnalysisTaskStateService.markFail(message.taskId(), ex.getErrorCode().name(), ex.getMessage());
            log.error("분석 작업 메시지 처리 실패. taskId={}, type={}, traceId={}, queue={}, deadLetterQueue={}, errorType={}, errorMessage={}, retryCount={}, durationMs={}",
                    message.taskId(),
                    message.type(),
                    message.traceId(),
                    properties.getQueue(),
                    properties.getDeadLetterQueue(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    retryCount,
                    duration.toMillis(),
                    ex);
            throw new AmqpRejectAndDontRequeueException(
                    "분석 작업 소비에 실패해 메시지를 DLQ로 보냅니다. taskId=" + message.taskId(),
                    ex
            );
        } catch (RuntimeException ex) {
            Duration duration = Duration.between(startedAt, Instant.now());
            if (ex instanceof AnalysisTaskAlreadyFailedException) {
                analysisTaskRabbitMetrics.recordConsumeFail(duration);
                log.error("분석 작업 메시지가 이미 FAIL 상태라 DLQ로 보냅니다. taskId={}, type={}, traceId={}, queue={}, deadLetterQueue={}, retryCount={}, durationMs={}",
                        message.taskId(),
                        message.type(),
                        message.traceId(),
                        properties.getQueue(),
                        properties.getDeadLetterQueue(),
                        retryCount,
                        duration.toMillis(),
                        ex);
                throw new AmqpRejectAndDontRequeueException(
                        "분석 작업이 이미 FAIL 상태라 메시지를 DLQ로 보냅니다. taskId=" + message.taskId(),
                        ex
                );
            }
            if (ex instanceof AnalysisTaskAlreadySucceededException || ex instanceof AnalysisTaskAlreadyRunningException) {
                analysisTaskRabbitMetrics.recordConsumeSuccess(duration);
                log.warn("분석 작업 메시지 중복 수신을 건너뜁니다. taskId={}, type={}, traceId={}, queue={}, retryCount={}, reason={}, durationMs={}",
                        message.taskId(),
                        message.type(),
                        message.traceId(),
                        properties.getQueue(),
                        retryCount,
                        ex.getMessage(),
                        duration.toMillis());
                return;
            }
            analysisTaskRabbitMetrics.recordConsumeFail(duration);
            externalAnalysisTaskStateService.markFail(
                    message.taskId(),
                    ErrorCode.INTERNAL_SERVER_ERROR.name(),
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
            );
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
                    "분석 작업 소비에 실패해 메시지를 DLQ로 보냅니다. taskId=" + message.taskId(),
                    ex
            );
        }
    }

    private boolean isRetryable(BaseException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return errorCode == ErrorCode.AI_SERVER_TIMEOUT
                || errorCode == ErrorCode.AI_SERVER_INTERNAL_ERROR
                || errorCode == ErrorCode.AI_SERVER_CONNECTION_FAILED;
    }

    private Long millisBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return null;
        }
        return Duration.between(from, to).toMillis();
    }
}
