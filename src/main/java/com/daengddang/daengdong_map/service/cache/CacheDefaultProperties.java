package com.daengddang.daengdong_map.service.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cache.default")
public class CacheDefaultProperties {

    private boolean enabled = true;
    private long ttlSeconds = 1800;
    private int jitterPercent = 20;
    private long operationTimeoutMs = 300;
    private String keyVersion = "v1";
}
