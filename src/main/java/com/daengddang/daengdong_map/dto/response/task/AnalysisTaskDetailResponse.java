package com.daengddang.daengdong_map.dto.response.task;

import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AnalysisTaskDetailResponse {

    private final String taskId;
    private final Long walkId;
    private final String type;
    private final String status;
    private final LocalDateTime requestedAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    private final String errorCode;
    private final String errorMessage;

    @Builder
    private AnalysisTaskDetailResponse(String taskId,
                                       Long walkId,
                                       String type,
                                       String status,
                                       LocalDateTime requestedAt,
                                       LocalDateTime startedAt,
                                       LocalDateTime finishedAt,
                                       String errorCode,
                                       String errorMessage) {
        this.taskId = taskId;
        this.walkId = walkId;
        this.type = type;
        this.status = status;
        this.requestedAt = requestedAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static AnalysisTaskDetailResponse from(ExternalAnalysisTask task) {
        return AnalysisTaskDetailResponse.builder()
                .taskId(task.getTaskId())
                .walkId(task.getWalk().getId())
                .type(task.getType().name())
                .status(task.getStatus().name())
                .requestedAt(task.getRequestedAt())
                .startedAt(task.getStartedAt())
                .finishedAt(task.getFinishedAt())
                .errorCode(task.getErrorCode())
                .errorMessage(task.getErrorMessage())
                .build();
    }
}
