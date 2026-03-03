package com.daengddang.daengdong_map.sse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisTaskSseRedisEnvelope {

    private String sourceNodeId;
    private String eventId;
    private String taskId;
    private String occurredAt;
    private String schemaVersion;
}
