package com.daengddang.daengdong_map.sse;

public interface AnalysisTaskEventDedupeStore {

    boolean tryMarkFirstProcessed(String eventId);
}
