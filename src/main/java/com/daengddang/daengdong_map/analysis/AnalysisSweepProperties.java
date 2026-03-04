package com.daengddang.daengdong_map.analysis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "analysis.sweep")
public class AnalysisSweepProperties {

    private boolean enabled = true;

    private long fixedDelayMs = 30000L;

    private long pendingTimeoutSeconds = 120L;

    private long runningTimeoutSeconds = 300L;

    private int batchSize = 200;
}
