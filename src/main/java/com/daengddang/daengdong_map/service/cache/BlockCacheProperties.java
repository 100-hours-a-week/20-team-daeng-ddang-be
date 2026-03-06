package com.daengddang.daengdong_map.service.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cache.block")
public class BlockCacheProperties {

    private Boolean enabled;
    private Long ttlSeconds;
    private Integer jitterPercent;
    private Integer invalidateMinRadiusMeters = 0;
    private Integer invalidateMaxRadiusMeters = 1500;
    private String keyVersion;
    private String key = "cache:blocks:list";
}
