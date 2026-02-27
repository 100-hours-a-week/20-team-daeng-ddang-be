package com.daengddang.daengdong_map.sse;

public enum  AnalysisTaskSseEventType {
    CONNECTED("connected"),
    STATUS("status"),
    HEARTBEAT("heartbeat");

    private final String eventName;

    AnalysisTaskSseEventType(String eventName) {
        this.eventName = eventName;
    }

    public String eventName() {
        return eventName;
    }
}
