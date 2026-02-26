package com.daengddang.daengdong_map.dto.response.healthcare;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class FastApiHealthcareAnalyzeResponse {

    @JsonProperty("analysis_id")
    private String analysisId;

    @JsonProperty("dog_id")
    private Integer dogId;

    @JsonProperty("analyze_at")
    private LocalDateTime analyzeAt;

    private Result result;
    private Metrics metrics;
    private Artifacts artifacts;

    @JsonProperty("error_code")
    private String errorCode;

    @Getter
    public static class Result {
        @JsonProperty("overall_risk_level")
        private String overallRiskLevel;
        private String summary;
    }

    @Getter
    public static class Metrics {
        @JsonProperty("patella_risk_signal")
        private MetricDetail patellaRiskSignal;

        @JsonProperty("gait_balance")
        private MetricDetail gaitBalance;

        @JsonProperty("knee_mobility")
        private MetricDetail kneeMobility;

        @JsonProperty("gait_stability")
        private MetricDetail gaitStability;

        @JsonProperty("gait_rhythm")
        private MetricDetail gaitRhythm;
    }

    @Getter
    public static class MetricDetail {
        private String level;
        private Integer score;
        private String description;
    }

    @Getter
    public static class Artifacts {
        @JsonProperty("keypoint_overlay_video_url")
        private String keypointOverlayVideoUrl;
    }
}
