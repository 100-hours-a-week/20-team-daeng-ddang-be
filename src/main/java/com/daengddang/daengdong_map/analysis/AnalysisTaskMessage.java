package com.daengddang.daengdong_map.analysis;

import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskType;
import java.time.LocalDateTime;

public record AnalysisTaskMessage(
        String taskId,
        ExternalAnalysisTaskType type,
        LocalDateTime requestedAt
) {

    public static AnalysisTaskMessage from(ExternalAnalysisTask task) {
        return new AnalysisTaskMessage(
                task.getTaskId(),
                task.getType(),
                task.getRequestedAt()
        );
    }
}
