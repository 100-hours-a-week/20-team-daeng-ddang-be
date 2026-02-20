package com.daengddang.daengdong_map.domain.task;

import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.walk.Walk;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "external_analysis_tasks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_external_analysis_tasks_task_id", columnNames = "task_id")
        },
        indexes = {
                @Index(name = "idx_external_analysis_tasks_walk_type_requested_at", columnList = "walk_id, type, requested_at"),
                @Index(name = "idx_external_analysis_tasks_dog_type_requested_at", columnList = "dog_id, type, requested_at"),
                @Index(name = "idx_external_analysis_tasks_status_requested_at", columnList = "status, requested_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "external_analysis_task_seq_generator",
        sequenceName = "external_analysis_tasks_external_analysis_task_id_seq",
        allocationSize = 1
)
public class ExternalAnalysisTask {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "external_analysis_task_seq_generator")
    @Column(name = "external_analysis_task_id")
    private Long id;

    @Column(name = "task_id", nullable = false, length = 64, updatable = false)
    private String taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ExternalAnalysisTaskType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ExternalAnalysisTaskStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Column(name = "result_type", length = 30)
    private String resultType;

    @Column(name = "result_id", length = 64)
    private String resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_id")
    private Walk walk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id", nullable = false)
    private Dog dog;

    @Builder
    private ExternalAnalysisTask(String taskId,
                                 ExternalAnalysisTaskType type,
                                 ExternalAnalysisTaskStatus status,
                                 LocalDateTime requestedAt,
                                 LocalDateTime startedAt,
                                 LocalDateTime finishedAt,
                                 String errorCode,
                                 String errorMessage,
                                 String videoUrl,
                                 String resultType,
                                 String resultId,
                                 Walk walk,
                                 Dog dog) {
        this.taskId = taskId;
        this.type = type;
        this.status = status;
        this.requestedAt = requestedAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.videoUrl = videoUrl;
        this.resultType = resultType;
        this.resultId = resultId;
        this.walk = walk;
        this.dog = dog;
    }

    @PrePersist
    private void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ExternalAnalysisTaskStatus.PENDING;
        }
    }
}
