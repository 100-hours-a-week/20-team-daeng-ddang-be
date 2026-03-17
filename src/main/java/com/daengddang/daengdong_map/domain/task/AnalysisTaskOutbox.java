package com.daengddang.daengdong_map.domain.task;

import com.daengddang.daengdong_map.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(
        name = "analysis_task_outbox",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_analysis_task_outbox_task_event",
                        columnNames = {"task_id", "event_type"}
                )
        },
        indexes = {
                @Index(name = "idx_analysis_task_outbox_status_created_at", columnList = "status, created_at"),
                @Index(name = "idx_analysis_task_outbox_task_id", columnList = "task_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "analysis_task_outbox_seq_generator",
        sequenceName = "analysis_task_outbox_outbox_id_seq",
        allocationSize = 1
)
public class AnalysisTaskOutbox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysis_task_outbox_seq_generator")
    @Column(name = "outbox_id")
    private Long id;

    @Column(name = "task_id", nullable = false, length = 64, updatable = false)
    private String taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50, updatable = false)
    private AnalysisTaskOutboxEventType eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnalysisTaskOutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Builder
    private AnalysisTaskOutbox(
            String taskId,
            AnalysisTaskOutboxEventType eventType,
            String payload,
            AnalysisTaskOutboxStatus status,
            int attemptCount,
            String lastError,
            LocalDateTime publishedAt,
            LocalDateTime nextAttemptAt
    ) {
        this.taskId = taskId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.attemptCount = attemptCount;
        this.lastError = lastError;
        this.publishedAt = publishedAt;
        this.nextAttemptAt = nextAttemptAt;
    }

    @PrePersist
    private void onCreate() {
        if (status == null) {
            status = AnalysisTaskOutboxStatus.PENDING;
        }
        if (nextAttemptAt == null) {
            nextAttemptAt = LocalDateTime.now();
        }
    }

    public void markPublished(LocalDateTime publishedAt) {
        this.status = AnalysisTaskOutboxStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.lastError = null;
        this.nextAttemptAt = null;
    }

    public void markFailed(String lastError, int maxAttempts, LocalDateTime nextAttemptAt) {
        this.attemptCount += 1;
        this.lastError = lastError;
        if (this.attemptCount >= maxAttempts) {
            this.status = AnalysisTaskOutboxStatus.FAILED;
            this.nextAttemptAt = null;
        } else {
            this.status = AnalysisTaskOutboxStatus.PENDING;
            this.nextAttemptAt = nextAttemptAt;
        }
    }
}
