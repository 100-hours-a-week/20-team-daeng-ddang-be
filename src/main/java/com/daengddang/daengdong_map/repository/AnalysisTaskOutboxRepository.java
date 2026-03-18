package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.task.AnalysisTaskOutbox;
import com.daengddang.daengdong_map.domain.task.AnalysisTaskOutboxEventType;
import com.daengddang.daengdong_map.domain.task.AnalysisTaskOutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisTaskOutboxRepository extends JpaRepository<AnalysisTaskOutbox, Long> {

    Optional<AnalysisTaskOutbox> findByTaskIdAndEventType(
            String taskId,
            AnalysisTaskOutboxEventType eventType
    );

    boolean existsByTaskIdAndEventType(
            String taskId,
            AnalysisTaskOutboxEventType eventType
    );

    @Query(value = """
            SELECT *
            FROM analysis_task_outbox
            WHERE status = 'PENDING'
              AND next_attempt_at <= now()
            ORDER BY created_at
            FOR UPDATE SKIP LOCKED
            LIMIT :batchSize
            """, nativeQuery = true)
    List<AnalysisTaskOutbox> findPendingBatch(@Param("batchSize") int batchSize);

    long countByStatus(AnalysisTaskOutboxStatus status);
}
