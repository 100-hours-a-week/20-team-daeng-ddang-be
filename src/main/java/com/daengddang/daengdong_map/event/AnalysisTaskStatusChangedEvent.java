package com.daengddang.daengdong_map.event;

import lombok.Getter;

@Getter
public class AnalysisTaskStatusChangedEvent {

    private final String taskId;

    public AnalysisTaskStatusChangedEvent(String taskId) {
        this.taskId = taskId;
    }
}
