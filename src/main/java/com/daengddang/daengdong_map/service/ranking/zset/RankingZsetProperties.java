package com.daengddang.daengdong_map.service.ranking.zset;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ranking.zset")
public class RankingZsetProperties {

    private boolean enabled = false;
    private String readSource = "db";
    private String keyVersion = "v3";
    private String keyPrefix = "rank";
    private long weekTtlSeconds = 691_200L;
    private long monthTtlSeconds = 3_024_000L;
    private long yearTtlSeconds = 7_776_000L;
    private long tempKeyTtlSeconds = 600L;
}
