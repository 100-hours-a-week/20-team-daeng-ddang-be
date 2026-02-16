package com.daengddang.daengdong_map.dto.response.healthcare;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class HealthcareAnalyzeResponse {

    @JsonProperty("healthcareId")
    private final Long healthcareId;

    @JsonProperty("analyzedAt")
    private final LocalDateTime analyzedAt;

    @JsonProperty("overallRiskLevel")
    private final String overallRiskLevel;

    private final String summary;
    private final Metrics metrics;
    private final Artifacts artifacts;

    @Builder
    private HealthcareAnalyzeResponse(Long healthcareId,
                                      LocalDateTime analyzedAt,
                                      String overallRiskLevel,
                                      String summary,
                                      Metrics metrics,
                                      Artifacts artifacts) {
        this.healthcareId = healthcareId;
        this.analyzedAt = analyzedAt;
        this.overallRiskLevel = overallRiskLevel;
        this.summary = summary;
        this.metrics = metrics;
        this.artifacts = artifacts;
    }

    public static HealthcareAnalyzeResponse of(Long healthcareId,
                                               LocalDateTime analyzedAt,
                                               String overallRiskLevel,
                                               String summary,
                                               Metrics metrics,
                                               Artifacts artifacts) {
        return HealthcareAnalyzeResponse.builder()
                .healthcareId(healthcareId)
                .analyzedAt(analyzedAt)
                .overallRiskLevel(overallRiskLevel)
                .summary(summary)
                .metrics(metrics)
                .artifacts(artifacts)
                .build();
    }

    @Getter
    public static class Metrics {
        private final MetricDetail patellaRiskSignal;
        private final MetricDetail gaitBalance;
        private final MetricDetail kneeMobility;
        private final MetricDetail gaitStability;
        private final MetricDetail gaitRhythm;

        @Builder
        private Metrics(MetricDetail patellaRiskSignal,
                        MetricDetail gaitBalance,
                        MetricDetail kneeMobility,
                        MetricDetail gaitStability,
                        MetricDetail gaitRhythm) {
            this.patellaRiskSignal = patellaRiskSignal;
            this.gaitBalance = gaitBalance;
            this.kneeMobility = kneeMobility;
            this.gaitStability = gaitStability;
            this.gaitRhythm = gaitRhythm;
        }

        public static Metrics of(MetricDetail patellaRiskSignal,
                                 MetricDetail gaitBalance,
                                 MetricDetail kneeMobility,
                                 MetricDetail gaitStability,
                                 MetricDetail gaitRhythm) {
            return Metrics.builder()
                    .patellaRiskSignal(patellaRiskSignal)
                    .gaitBalance(gaitBalance)
                    .kneeMobility(kneeMobility)
                    .gaitStability(gaitStability)
                    .gaitRhythm(gaitRhythm)
                    .build();
        }
    }

    @Getter
    public static class MetricDetail {
        private final String level;
        private final Integer score;
        private final String description;

        @Builder
        private MetricDetail(String level, Integer score, String description) {
            this.level = level;
            this.score = score;
            this.description = description;
        }

        public static MetricDetail of(String level, Integer score, String description) {
            return MetricDetail.builder()
                    .level(level)
                    .score(score)
                    .description(description)
                    .build();
        }
    }

    @Getter
    public static class Artifacts {
        private final String keypointOverlayVideoUrl;

        @Builder
        private Artifacts(String keypointOverlayVideoUrl) {
            this.keypointOverlayVideoUrl = keypointOverlayVideoUrl;
        }

        public static Artifacts of(String keypointOverlayVideoUrl) {
            return Artifacts.builder()
                    .keypointOverlayVideoUrl(keypointOverlayVideoUrl)
                    .build();
        }
    }
}
