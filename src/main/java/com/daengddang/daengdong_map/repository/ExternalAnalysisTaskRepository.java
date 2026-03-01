package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskStatus;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskType;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalAnalysisTaskRepository extends JpaRepository<ExternalAnalysisTask, Long> {

    Optional<ExternalAnalysisTask> findByTaskId(String taskId);

    @EntityGraph(attributePaths = {"walk", "dog", "dog.user"})
    Optional<ExternalAnalysisTask> findWithContextByTaskId(String taskId);

    Optional<ExternalAnalysisTask> findByWalkIdAndTaskId(Long walkId, String taskId);

    Optional<ExternalAnalysisTask> findByDogIdAndTaskId(Long dogId, String taskId);

    Optional<ExternalAnalysisTask> findTopByWalkIdAndTypeOrderByRequestedAtDescIdDesc(
            Long walkId,
            ExternalAnalysisTaskType type
    );

    Optional<ExternalAnalysisTask> findTopByWalkIdAndTypeAndStatusInOrderByRequestedAtDescIdDesc(
            Long walkId,
            ExternalAnalysisTaskType type,
            List<ExternalAnalysisTaskStatus> statuses
    );

    Optional<ExternalAnalysisTask> findTopByDogIdAndTypeAndStatusInOrderByRequestedAtDescIdDesc(
            Long dogId,
            ExternalAnalysisTaskType type,
            List<ExternalAnalysisTaskStatus> statuses
    );

    List<ExternalAnalysisTask> findByStatusOrderByRequestedAtAsc(
            ExternalAnalysisTaskStatus status,
            Pageable pageable
    );

    default Optional<ExternalAnalysisTask> findLatestByWalkIdAndType(Long walkId, ExternalAnalysisTaskType type) {
        return findTopByWalkIdAndTypeOrderByRequestedAtDescIdDesc(walkId, type);
    }

    default Optional<ExternalAnalysisTask> findLatestActiveByWalkIdAndType(Long walkId, ExternalAnalysisTaskType type) {
        return findTopByWalkIdAndTypeAndStatusInOrderByRequestedAtDescIdDesc(
                walkId,
                type,
                List.of(ExternalAnalysisTaskStatus.PENDING, ExternalAnalysisTaskStatus.RUNNING)
        );
    }

    default Optional<ExternalAnalysisTask> findLatestActiveByDogIdAndType(Long dogId, ExternalAnalysisTaskType type) {
        return findTopByDogIdAndTypeAndStatusInOrderByRequestedAtDescIdDesc(
                dogId,
                type,
                List.of(ExternalAnalysisTaskStatus.PENDING, ExternalAnalysisTaskStatus.RUNNING)
        );
    }

    default List<ExternalAnalysisTask> findPendingBatch(int batchSize) {
        return findByStatusOrderByRequestedAtAsc(
                ExternalAnalysisTaskStatus.PENDING,
                PageRequest.of(0, batchSize)
        );
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ExternalAnalysisTask task
               set task.status = :running,
                   task.startedAt = :startedAt,
                   task.errorCode = null,
                   task.errorMessage = null
             where task.taskId = :taskId
               and task.status = :pending
            """)
    int markRunningIfPending(
            @Param("taskId") String taskId,
            @Param("pending") ExternalAnalysisTaskStatus pending,
            @Param("running") ExternalAnalysisTaskStatus running,
            @Param("startedAt") LocalDateTime startedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ExternalAnalysisTask task
               set task.status = :success,
                   task.finishedAt = :finishedAt,
                   task.resultType = :resultType,
                   task.resultId = :resultId
             where task.taskId = :taskId
               and task.status = :running
            """)
    int markSuccessIfRunning(
            @Param("taskId") String taskId,
            @Param("running") ExternalAnalysisTaskStatus running,
            @Param("success") ExternalAnalysisTaskStatus success,
            @Param("finishedAt") LocalDateTime finishedAt,
            @Param("resultType") String resultType,
            @Param("resultId") String resultId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ExternalAnalysisTask task
               set task.status = :fail,
                   task.finishedAt = :finishedAt,
                   task.errorCode = :errorCode,
                   task.errorMessage = :errorMessage
             where task.taskId = :taskId
               and task.status in :statuses
            """)
    int markFail(
            @Param("taskId") String taskId,
            @Param("statuses") List<ExternalAnalysisTaskStatus> statuses,
            @Param("fail") ExternalAnalysisTaskStatus fail,
            @Param("finishedAt") LocalDateTime finishedAt,
            @Param("errorCode") String errorCode,
            @Param("errorMessage") String errorMessage
    );
}
