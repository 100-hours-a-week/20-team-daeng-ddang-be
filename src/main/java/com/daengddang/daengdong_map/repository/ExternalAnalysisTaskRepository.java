package com.daengddang.daengdong_map.repository;

import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskStatus;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalAnalysisTaskRepository extends JpaRepository<ExternalAnalysisTask, Long> {

    Optional<ExternalAnalysisTask> findByTaskId(String taskId);

    Optional<ExternalAnalysisTask> findTopByWalk_IdAndTypeOrderByRequestedAtDescIdDesc(
            Long walkId,
            ExternalAnalysisTaskType type
    );

    List<ExternalAnalysisTask> findByStatusOrderByRequestedAtAsc(
            ExternalAnalysisTaskStatus status,
            Pageable pageable
    );

    default Optional<ExternalAnalysisTask> findLatestByWalkIdAndType(Long walkId, ExternalAnalysisTaskType type) {
        return findTopByWalk_IdAndTypeOrderByRequestedAtDescIdDesc(walkId, type);
    }

    default List<ExternalAnalysisTask> findPendingBatch(int batchSize) {
        return findByStatusOrderByRequestedAtAsc(
                ExternalAnalysisTaskStatus.PENDING,
                PageRequest.of(0, batchSize)
        );
    }
}
