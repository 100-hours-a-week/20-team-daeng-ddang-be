package com.daengddang.daengdong_map.sse;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "async.sse.redis")
public class AsyncSseRedisProperties {

    private boolean enabled = true;
    private String channel = "analysis-task-status-events";
    private String nodeId = "local-node";
}
