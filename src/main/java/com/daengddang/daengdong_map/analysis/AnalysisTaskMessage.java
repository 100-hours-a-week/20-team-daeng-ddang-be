package com.daengddang.daengdong_map.analysis;

import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskType;
import java.time.LocalDateTime;
import java.util.UUID;

public record AnalysisTaskMessage(
        String taskId,
        ExternalAnalysisTaskType type,
        LocalDateTime requestedAt,
        String version,
        LocalDateTime publishedAt,
        String traceId
) {

    private static final String CURRENT_VERSION = "v3";

    public static AnalysisTaskMessage from(ExternalAnalysisTask task) {
        return new AnalysisTaskMessage(
                task.getTaskId(),
                task.getType(),
                task.getRequestedAt(),
                CURRENT_VERSION,
                LocalDateTime.now(),
                UUID.randomUUID().toString()
        );
    }
}
