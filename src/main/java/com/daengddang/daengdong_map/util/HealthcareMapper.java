package com.daengddang.daengdong_map.util;

import com.daengddang.daengdong_map.domain.analysis.Analysis;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.dto.request.healthcare.FastApiHealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.healthcare.HealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.healthcare.FastApiHealthcareAnalyzeResponse;
import com.daengddang.daengdong_map.dto.response.healthcare.HealthcareAnalyzeResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class HealthcareMapper {

    public FastApiHealthcareAnalyzeRequest toFastApiRequest(HealthcareAnalyzeRequest request, Dog dog) {
        return FastApiHealthcareAnalyzeRequest.of(
                request.getVideoUrl(),
                UUID.randomUUID().toString(),
                Math.toIntExact(dog.getId())
        );
    }

    public Analysis toAnalysis(FastApiHealthcareAnalyzeResponse response, HealthcareAnalyzeRequest request, Dog dog) {
        FastApiHealthcareAnalyzeResponse.Metrics metrics = response.getMetrics();
        return Analysis.builder()
                .summary(response.getResult().getSummary())
                .riskLevel(response.getResult().getOverallRiskLevel())
                .videoUrl(request.getVideoUrl())
                .patellaRiskScore(metrics.getPatellaRiskSignal().getScore())
                .patellaRiskDesc(metrics.getPatellaRiskSignal().getDescription())
                .patellaRiskLevel(metrics.getPatellaRiskSignal().getLevel())
                .gaitBalanceScore(metrics.getGaitBalance().getScore())
                .gaitBalanceDesc(metrics.getGaitBalance().getDescription())
                .gaitBalanceLevel(metrics.getGaitBalance().getLevel())
                .kneeMobilityScore(metrics.getKneeMobility().getScore())
                .kneeMobilityDesc(metrics.getKneeMobility().getDescription())
                .kneeMobilityLevel(metrics.getKneeMobility().getLevel())
                .gaitStabilityScore(metrics.getGaitStability().getScore())
                .gaitStabilityDesc(metrics.getGaitStability().getDescription())
                .gaitStabilityLevel(metrics.getGaitStability().getLevel())
                .gaitRhythmScore(metrics.getGaitRhythm().getScore())
                .gaitRhythmDesc(metrics.getGaitRhythm().getDescription())
                .gaitRhythmLevel(metrics.getGaitRhythm().getLevel())
                .keypointOverlayVideoUrl(
                        response.getArtifacts() == null
                                ? null
                                : response.getArtifacts().getKeypointOverlayVideoUrl()
                )
                .createdAt(toLocalDateTime(response.getAnalyzeAt()))
                .dog(dog)
                .build();
    }

    public HealthcareAnalyzeResponse toPublicResponse(Analysis analysis) {
        return HealthcareAnalyzeResponse.of(
                analysis.getId(),
                analysis.getCreatedAt(),
                analysis.getRiskLevel(),
                analysis.getSummary(),
                HealthcareAnalyzeResponse.Metrics.of(
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                analysis.getPatellaRiskLevel(),
                                analysis.getPatellaRiskScore(),
                                analysis.getPatellaRiskDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                analysis.getGaitBalanceLevel(),
                                analysis.getGaitBalanceScore(),
                                analysis.getGaitBalanceDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                analysis.getKneeMobilityLevel(),
                                analysis.getKneeMobilityScore(),
                                analysis.getKneeMobilityDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                analysis.getGaitStabilityLevel(),
                                analysis.getGaitStabilityScore(),
                                analysis.getGaitStabilityDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                analysis.getGaitRhythmLevel(),
                                analysis.getGaitRhythmScore(),
                                analysis.getGaitRhythmDesc()
                        )
                ),
                HealthcareAnalyzeResponse.Artifacts.of(analysis.getKeypointOverlayVideoUrl())
        );
    }

    public HealthcareAnalyzeResponse toPublicResponse(Analysis analysis, FastApiHealthcareAnalyzeResponse response) {
        return HealthcareAnalyzeResponse.of(
                analysis.getId(),
                analysis.getCreatedAt(),
                analysis.getRiskLevel(),
                analysis.getSummary(),
                HealthcareAnalyzeResponse.Metrics.of(
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                analysis.getPatellaRiskLevel(),
                                analysis.getPatellaRiskScore(),
                                analysis.getPatellaRiskDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                analysis.getGaitBalanceLevel(),
                                analysis.getGaitBalanceScore(),
                                analysis.getGaitBalanceDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                analysis.getKneeMobilityLevel(),
                                analysis.getKneeMobilityScore(),
                                analysis.getKneeMobilityDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                analysis.getGaitStabilityLevel(),
                                analysis.getGaitStabilityScore(),
                                analysis.getGaitStabilityDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                analysis.getGaitRhythmLevel(),
                                analysis.getGaitRhythmScore(),
                                analysis.getGaitRhythmDesc()
                        )
                ),
                HealthcareAnalyzeResponse.Artifacts.of(analysis.getKeypointOverlayVideoUrl())
        );
    }

    private LocalDateTime toLocalDateTime(LocalDateTime analyzedAt) {
        if (analyzedAt == null) {
            return LocalDateTime.now();
        }
        return analyzedAt;
    }
}
