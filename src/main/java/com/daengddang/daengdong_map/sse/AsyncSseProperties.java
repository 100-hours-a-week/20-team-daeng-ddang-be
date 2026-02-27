package com.daengddang.daengdong_map.sse;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "async.sse")
public class AsyncSseProperties {

    private boolean enabled = false;
    private long emitterTimeoutMs = 180_000L;
    private long heartbeatIntervalMs = 15_000L;
    private Executor executor = new Executor();

    @Getter
    @Setter
    public static class Executor {
        private int corePoolSize = 2;
        private int maxPoolSize = 8;
        private int queueCapacity = 500;
    }
}
