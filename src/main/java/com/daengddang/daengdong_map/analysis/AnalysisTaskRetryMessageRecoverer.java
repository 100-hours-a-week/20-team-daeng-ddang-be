package com.daengddang.daengdong_map.analysis;

import com.daengddang.daengdong_map.common.exception.RetryableAnalysisTaskException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisTaskRetryMessageRecoverer implements MessageRecoverer {

    public static final String RETRY_COUNT_HEADER = "x-analysis-retry-count";

    private final RabbitTemplate rabbitTemplate;
    private final AnalysisRabbitMqProperties properties;

    @Override
    public void recover(Message message, Throwable cause) {
        log.warn("retry recoverer invoked. causeType={}, messageId={}, retryCount={}",
                cause == null ? "null" : cause.getClass().getName(),
                message.getMessageProperties().getMessageId(),
                readRetryCount(message));

        RetryableAnalysisTaskException retryableException = findRetryableCause(cause);
        if (retryableException == null) {
            log.error("retry recoverer will dead-letter message because retryable cause was not found. causeType={}",
                    cause == null ? "null" : cause.getClass().getName(),
                    cause);
            throw new AmqpRejectAndDontRequeueException(
                    "analysis task consumer failed and message will be dead-lettered",
                    cause
            );
        }

        int currentRetryCount = readRetryCount(message);
        if (currentRetryCount >= properties.getRetryMaxAttempts()) {
            log.error("retry recoverer exhausted retry attempts. currentRetryCount={}, maxRetryAttempts={}",
                    currentRetryCount,
                    properties.getRetryMaxAttempts(),
                    cause);
            throw new AmqpRejectAndDontRequeueException(
                    "analysis task retry attempts exhausted and message will be dead-lettered",
                    cause
            );
        }

        int nextRetryCount = currentRetryCount + 1;
        Message retryMessage = MessageBuilder.fromClonedMessage(message)
                .setHeader(RETRY_COUNT_HEADER, nextRetryCount)
                .build();

        rabbitTemplate.send(properties.getRetryExchange(), properties.getRetryRoutingKey(), retryMessage);
        log.warn("분석 작업 메시지를 retry queue로 이동합니다. retryCount={}, retryExchange={}, retryQueue={}, cause={}",
                nextRetryCount,
                properties.getRetryExchange(),
                properties.getRetryQueue(),
                retryableException.getMessage());
    }

    public static int readRetryCount(Message message) {
        Object headerValue = message.getMessageProperties().getHeaders().get(RETRY_COUNT_HEADER);
        if (headerValue instanceof Number number) {
            return number.intValue();
        }
        if (headerValue instanceof String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private RetryableAnalysisTaskException findRetryableCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof RetryableAnalysisTaskException retryableException) {
                return retryableException;
            }
            current = current.getCause();
        }
        return null;
    }
}
