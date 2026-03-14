package com.daengddang.daengdong_map.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "ai.fastapi")
public class FastApiProperties {

    private String missionJudgeUri;
    private String expressionAnalyzeUri;
    private String healthcareAnalyzeUri;
    private String healthcareChatUri;
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(80);
    private Duration missionHardTimeout = Duration.ofSeconds(80);
    private Duration expressionHardTimeout = Duration.ofSeconds(15);
    private Duration healthcareHardTimeout = Duration.ofSeconds(20);
    private Duration chatHardTimeout = Duration.ofSeconds(10);
}
