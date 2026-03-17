package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.analysis.AnalysisTaskMessage;
import com.daengddang.daengdong_map.analysis.AnalysisTaskRabbitPublisher;
import com.daengddang.daengdong_map.domain.task.AnalysisTaskOutbox;
import com.daengddang.daengdong_map.domain.task.AnalysisTaskOutboxStatus;
import com.daengddang.daengdong_map.repository.AnalysisTaskOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "analysis.outbox", name = "enabled", havingValue = "true")
public class AnalysisTaskOutboxPublisherScheduler {

    private final AnalysisTaskOutboxRepository analysisTaskOutboxRepository;
    private final AnalysisTaskRabbitPublisher analysisTaskRabbitPublisher;
    private final AnalysisTaskOutboxMetrics analysisTaskOutboxMetrics;
    private final ObjectMapper objectMapper;

    @Value("${analysis.outbox.batch-size:100}")
    private int batchSize;

    @Value("${analysis.outbox.max-attempts:5}")
    private int maxAttempts;

    @Value("${analysis.outbox.retry-base-delay-ms:5000}")
    private long retryBaseDelayMs;

    @Value("${analysis.outbox.retry-max-delay-ms:60000}")
    private long retryMaxDelayMs;

    @Scheduled(fixedDelayString = "${analysis.outbox.fixed-delay-ms:1000}")
    @Transactional
    public void publishPendingOutbox() {
        List<AnalysisTaskOutbox> pendingBatch = analysisTaskOutboxRepository.findPendingBatch(batchSize);
        analysisTaskOutboxMetrics.updatePendingCount((int) analysisTaskOutboxRepository.countByStatus(AnalysisTaskOutboxStatus.PENDING));
        analysisTaskOutboxMetrics.updateFailedCount((int) analysisTaskOutboxRepository.countByStatus(AnalysisTaskOutboxStatus.FAILED));
        if (pendingBatch.isEmpty()) {
            return;
        }

        for (AnalysisTaskOutbox outbox : pendingBatch) {
            long startedAt = System.nanoTime();
            try {
                AnalysisTaskMessage message = objectMapper.readValue(outbox.getPayload(), AnalysisTaskMessage.class);
                analysisTaskRabbitPublisher.publish(message);
                outbox.markPublished(LocalDateTime.now());
                analysisTaskOutboxMetrics.recordPublishSuccess(Duration.ofNanos(System.nanoTime() - startedAt));
            } catch (Exception ex) {
                outbox.markFailed(
                        truncate(ex.getMessage()),
                        maxAttempts,
                        nextAttemptAt(outbox.getAttemptCount() + 1)
                );
                analysisTaskOutboxMetrics.recordPublishFail(Duration.ofNanos(System.nanoTime() - startedAt));
                log.error("analysis task outbox publish failed. outboxId={}, taskId={}, attemptCount={}",
                        outbox.getId(), outbox.getTaskId(), outbox.getAttemptCount(), ex);
            }
        }

        analysisTaskOutboxMetrics.updatePendingCount((int) analysisTaskOutboxRepository.countByStatus(AnalysisTaskOutboxStatus.PENDING));
        analysisTaskOutboxMetrics.updateFailedCount((int) analysisTaskOutboxRepository.countByStatus(AnalysisTaskOutboxStatus.FAILED));
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        if (value.length() <= 2000) {
            return value;
        }
        return value.substring(0, 2000);
    }

    private LocalDateTime nextAttemptAt(int nextAttemptCount) {
        long delayMs = Math.min(retryBaseDelayMs * (long) nextAttemptCount, retryMaxDelayMs);
        return LocalDateTime.now().plusNanos(delayMs * 1_000_000L);
    }
}
