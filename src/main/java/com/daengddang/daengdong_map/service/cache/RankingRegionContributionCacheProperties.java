package com.daengddang.daengdong_map.service.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cache.ranking.contribution")
public class RankingRegionContributionCacheProperties {

    private Boolean enabled;
    private Long ttlSeconds;
    private Integer jitterPercent;
    private String keyVersion;
    private String summaryKey = "cache:rankings:regions:contributions:summary";
    private String listKey = "cache:rankings:regions:contributions:list";
}
