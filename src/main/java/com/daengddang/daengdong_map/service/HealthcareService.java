package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.ai.FastApiClient;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.analysis.Analysis;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.dto.request.healthcare.FastApiHealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.healthcare.HealthcareAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.healthcare.FastApiHealthcareAnalyzeResponse;
import com.daengddang.daengdong_map.dto.response.healthcare.HealthcareAnalyzeResponse;
import com.daengddang.daengdong_map.repository.AnalysisRepository;
import com.daengddang.daengdong_map.util.AccessValidator;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HealthcareService {

    private final AccessValidator accessValidator;
    private final FastApiClient fastApiClient;
    private final AnalysisRepository analysisRepository;

    @Transactional
    public HealthcareAnalyzeResponse analyze(Long userId, HealthcareAnalyzeRequest dto) {
        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        Dog dog = accessValidator.getDogOrThrow(userId);
        String fastApiAnalysisId = UUID.randomUUID().toString();

        FastApiHealthcareAnalyzeRequest fastApiRequest = FastApiHealthcareAnalyzeRequest.of(
                dto.getVideoUrl(),
                fastApiAnalysisId,
                Math.toIntExact(dog.getId())
        );
        FastApiHealthcareAnalyzeResponse fastApiResponse = fastApiClient.requestHealthcareAnalyze(fastApiRequest);

        validateFastApiResponse(fastApiResponse, dog);

        FastApiHealthcareAnalyzeResponse.Metrics fastApiMetrics = fastApiResponse.getMetrics();
        Analysis saved = analysisRepository.save(
                Analysis.builder()
                        .summary(fastApiResponse.getResult().getSummary())
                        .riskLevel(fastApiResponse.getResult().getOverallRiskLevel())
                        .videoUrl(dto.getVideoUrl())
                        .patellaRiskScore(fastApiMetrics.getPatellaRiskSignal().getScore())
                        .patellaRiskDesc(fastApiMetrics.getPatellaRiskSignal().getDescription())
                        .gaitBalanceScore(fastApiMetrics.getGaitBalance().getScore())
                        .gaitBalanceDesc(fastApiMetrics.getGaitBalance().getDescription())
                        .kneeMobilityScore(fastApiMetrics.getKneeMobility().getScore())
                        .kneeMobilityDesc(fastApiMetrics.getKneeMobility().getDescription())
                        .gaitStabilityScore(fastApiMetrics.getGaitStability().getScore())
                        .gaitStabilityDesc(fastApiMetrics.getGaitStability().getDescription())
                        .gaitRhythmScore(fastApiMetrics.getGaitRhythm().getScore())
                        .gaitRhythmDesc(fastApiMetrics.getGaitRhythm().getDescription())
                        .keypointOverlayVideoUrl(
                                fastApiResponse.getArtifacts() == null
                                        ? null
                                        : fastApiResponse.getArtifacts().getKeypointOverlayVideoUrl()
                        )
                        .createdAt(toLocalDateTime(fastApiResponse.getAnalyzeAt()))
                        .dog(dog)
                        .build()
        );

        return toResponse(saved, fastApiResponse);
    }

    @Transactional(readOnly = true)
    public HealthcareAnalyzeResponse getHealthcare(Long userId, Long healthcareId) {
        accessValidator.getUserOrThrow(userId);
        Analysis analysis = analysisRepository.findById(healthcareId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!userId.equals(analysis.getDog().getUser().getId())) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }

        return toResponse(analysis);
    }

    private void validateFastApiResponse(FastApiHealthcareAnalyzeResponse response, Dog dog) {
        if (response == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        if (response.getErrorCode() != null && !response.getErrorCode().isBlank()) {
            throw toErrorCodeException(response.getErrorCode());
        }

        if (response.getDogId() == null || response.getDogId().longValue() != dog.getId()) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        if (response.getResult() == null
                || response.getResult().getOverallRiskLevel() == null
                || response.getResult().getSummary() == null
                || response.getMetrics() == null
                || response.getMetrics().getPatellaRiskSignal() == null
                || response.getMetrics().getGaitBalance() == null
                || response.getMetrics().getKneeMobility() == null
                || response.getMetrics().getGaitStability() == null
                || response.getMetrics().getGaitRhythm() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        validateMetric(response.getMetrics().getPatellaRiskSignal());
        validateMetric(response.getMetrics().getGaitBalance());
        validateMetric(response.getMetrics().getKneeMobility());
        validateMetric(response.getMetrics().getGaitStability());
        validateMetric(response.getMetrics().getGaitRhythm());
    }

    private void validateMetric(FastApiHealthcareAnalyzeResponse.MetricDetail metric) {
        if (metric.getScore() == null || metric.getDescription() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }
    }

    private BaseException toErrorCodeException(String errorCodeText) {
        try {
            return new BaseException(ErrorCode.valueOf(errorCodeText));
        } catch (IllegalArgumentException ignored) {
            return new BaseException(ErrorCode.AI_SERVER_CONNECTION_FAILED);
        }
    }

    private LocalDateTime toLocalDateTime(java.time.OffsetDateTime analyzedAt) {
        if (analyzedAt == null) {
            return LocalDateTime.now();
        }
        return analyzedAt.toLocalDateTime();
    }

    private HealthcareAnalyzeResponse toResponse(Analysis analysis) {
        return HealthcareAnalyzeResponse.of(
                analysis.getId(),
                analysis.getCreatedAt(),
                analysis.getRiskLevel(),
                analysis.getSummary(),
                HealthcareAnalyzeResponse.Metrics.of(
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                null,
                                analysis.getPatellaRiskScore(),
                                analysis.getPatellaRiskDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                null,
                                analysis.getGaitBalanceScore(),
                                analysis.getGaitBalanceDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                null,
                                analysis.getKneeMobilityScore(),
                                analysis.getKneeMobilityDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                null,
                                analysis.getGaitStabilityScore(),
                                analysis.getGaitStabilityDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                null,
                                analysis.getGaitRhythmScore(),
                                analysis.getGaitRhythmDesc()
                        )
                ),
                HealthcareAnalyzeResponse.Artifacts.of(analysis.getKeypointOverlayVideoUrl())
        );
    }

    private HealthcareAnalyzeResponse toResponse(Analysis analysis, FastApiHealthcareAnalyzeResponse fastApiResponse) {
        return HealthcareAnalyzeResponse.of(
                analysis.getId(),
                analysis.getCreatedAt(),
                analysis.getRiskLevel(),
                analysis.getSummary(),
                HealthcareAnalyzeResponse.Metrics.of(
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                fastApiResponse.getMetrics().getPatellaRiskSignal().getLevel(),
                                analysis.getPatellaRiskScore(),
                                analysis.getPatellaRiskDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                fastApiResponse.getMetrics().getGaitBalance().getLevel(),
                                analysis.getGaitBalanceScore(),
                                analysis.getGaitBalanceDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                fastApiResponse.getMetrics().getKneeMobility().getLevel(),
                                analysis.getKneeMobilityScore(),
                                analysis.getKneeMobilityDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                fastApiResponse.getMetrics().getGaitStability().getLevel(),
                                analysis.getGaitStabilityScore(),
                                analysis.getGaitStabilityDesc()
                        ),
                        HealthcareAnalyzeResponse.MetricDetail.of(
                                fastApiResponse.getMetrics().getGaitRhythm().getLevel(),
                                analysis.getGaitRhythmScore(),
                                analysis.getGaitRhythmDesc()
                        )
                ),
                HealthcareAnalyzeResponse.Artifacts.of(
                        analysis.getKeypointOverlayVideoUrl()
                )
        );
    }
}
