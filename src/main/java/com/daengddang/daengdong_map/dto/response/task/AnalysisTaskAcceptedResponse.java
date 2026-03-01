package com.daengddang.daengdong_map.dto.response.task;

import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTask;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AnalysisTaskAcceptedResponse {

    private final String taskId;
    private final String status;

    @Builder
    private AnalysisTaskAcceptedResponse(String taskId, String status) {
        this.taskId = taskId;
        this.status = status;
    }

    public static AnalysisTaskAcceptedResponse from(ExternalAnalysisTask task) {
        return AnalysisTaskAcceptedResponse.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus().name())
                .build();
    }
}
