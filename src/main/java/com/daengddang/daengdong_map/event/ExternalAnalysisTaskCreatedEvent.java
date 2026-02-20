package com.daengddang.daengdong_map.event;

import com.daengddang.daengdong_map.domain.task.ExternalAnalysisTaskType;
import lombok.Getter;

@Getter
public class ExternalAnalysisTaskCreatedEvent {

    private final String taskId;
    private final ExternalAnalysisTaskType type;

    public ExternalAnalysisTaskCreatedEvent(String taskId, ExternalAnalysisTaskType type) {
        this.taskId = taskId;
        this.type = type;
    }
}
