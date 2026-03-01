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
import com.daengddang.daengdong_map.util.HealthcareMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HealthcareService {

    private final AccessValidator accessValidator;
    private final FastApiClient fastApiClient;
    private final AnalysisRepository analysisRepository;
    private final HealthcareMapper healthcareMapper;

    @Transactional
    public HealthcareAnalyzeResponse analyze(Long userId, HealthcareAnalyzeRequest dto) {
        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        Dog dog = accessValidator.getDogOrThrow(userId);
        FastApiHealthcareAnalyzeRequest fastApiRequest = healthcareMapper.toFastApiRequest(dto, dog);
        FastApiHealthcareAnalyzeResponse fastApiResponse = fastApiClient.requestHealthcareAnalyze(fastApiRequest);

        validateFastApiResponse(fastApiResponse, dog);

        Analysis saved = analysisRepository.save(healthcareMapper.toAnalysis(fastApiResponse, dto, dog));

        return healthcareMapper.toPublicResponse(saved, fastApiResponse);
    }

    @Transactional(readOnly = true)
    public HealthcareAnalyzeResponse getHealthcare(Long userId, Long healthcareId) {
        accessValidator.getUserOrThrow(userId);
        Analysis analysis = analysisRepository.findById(healthcareId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!userId.equals(analysis.getDog().getUser().getId())) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }

        return healthcareMapper.toPublicResponse(analysis);
    }

    private void validateFastApiResponse(FastApiHealthcareAnalyzeResponse response, Dog dog) {
        if (response == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
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
}
