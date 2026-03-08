package com.daengddang.daengdong_map.config;

import com.daengddang.daengdong_map.analysis.AnalysisBackpressureProperties;
import com.daengddang.daengdong_map.analysis.AnalysisSweepProperties;
import com.daengddang.daengdong_map.ai.FastApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        FastApiProperties.class,
        AnalysisBackpressureProperties.class,
        AnalysisSweepProperties.class
})
public class FastApiConfig {
}
