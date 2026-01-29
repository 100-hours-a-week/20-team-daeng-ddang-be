package com.daengddang.daengdong_map.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ai.fastapi")
public class FastApiProperties {

    private String missionJudgeUri;
    private String faceAnalyzeUri;
}
