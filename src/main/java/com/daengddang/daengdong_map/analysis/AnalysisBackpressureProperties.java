package com.daengddang.daengdong_map.analysis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "analysis.backpressure")
public class AnalysisBackpressureProperties {

    private boolean enabled = true;

    private int maxActiveTasks = 180;

    private int retryAfterSeconds = 3;
}
