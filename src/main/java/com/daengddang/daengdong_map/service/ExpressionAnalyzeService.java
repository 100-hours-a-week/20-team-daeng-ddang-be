package com.daengddang.daengdong_map.service;

import com.daengddang.daengdong_map.ai.FastApiClient;
import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.exception.BaseException;
import com.daengddang.daengdong_map.domain.dog.Dog;
import com.daengddang.daengdong_map.domain.expression.Expression;
import com.daengddang.daengdong_map.domain.walk.Walk;
import com.daengddang.daengdong_map.dto.request.expression.ExpressionAnalyzeRequest;
import com.daengddang.daengdong_map.dto.request.expression.FastApiExpressionAnalyzeRequest;
import com.daengddang.daengdong_map.dto.response.expression.ExpressionAnalyzeResponse;
import com.daengddang.daengdong_map.dto.response.expression.FastApiExpressionAnalyzeResponse;
import com.daengddang.daengdong_map.repository.ExpressionRepository;
import com.daengddang.daengdong_map.util.AccessValidator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpressionAnalyzeService {

    private final AccessValidator accessValidator;
    private final ExpressionRepository expressionRepository;
    private final FastApiClient fastApiClient;

    @Transactional
    public ExpressionAnalyzeResponse analyze(Long userId, Long walkId, ExpressionAnalyzeRequest dto) {

        if (dto == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        Walk walk = accessValidator.getOwnedWalkOrThrow(userId, walkId);
        Dog dog = walk.getDog();

        String analysisId = UUID.randomUUID().toString();
        FastApiExpressionAnalyzeRequest fastApiRequest =
                FastApiExpressionAnalyzeRequest.of(analysisId, dto.getVideoUrl());
        FastApiExpressionAnalyzeResponse fastApiResponse =
                fastApiClient.requestExpressionAnalyze(fastApiRequest);

        Expression expression = expressionRepository.findByWalk(walk)
                .orElseGet(() -> toExpression(
                        fastApiResponse,
                        dto.getVideoUrl(),
                        dog,
                        walk
                ));

        if (expression.getId() == null) {
            expressionRepository.save(expression);
        } else {
            updateExpression(expression, fastApiResponse, dto.getVideoUrl());
        }

        return toResponse(fastApiResponse, analysisId);
    }

    private Expression toExpression(
            FastApiExpressionAnalyzeResponse response,
            String videoUrl,
            Dog dog,
            Walk walk
    ) {
        if (response == null
                || response.getEmotionProbabilities() == null
                || response.getSummary() == null
                || response.getPredictedEmotion() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        FastApiExpressionAnalyzeResponse.EmotionProbabilities probabilities =
                response.getEmotionProbabilities();

        return Expression.builder()
                .videoUrl(videoUrl)
                .summary(response.getSummary())
                .predictedEmotion(response.getPredictedEmotion())
                .angry(probabilities.getAngry())
                .happy(probabilities.getHappy())
                .sad(probabilities.getSad())
                .relaxed(probabilities.getRelaxed())
                .dog(dog)
                .walk(walk)
                .build();
    }

    private void updateExpression(Expression expression,
                                  FastApiExpressionAnalyzeResponse response,
                                  String videoUrl) {
        FastApiExpressionAnalyzeResponse.EmotionProbabilities probabilities =
                response.getEmotionProbabilities();
        expression.updateResult(
                videoUrl,
                response.getSummary(),
                response.getPredictedEmotion(),
                probabilities.getAngry(),
                probabilities.getHappy(),
                probabilities.getSad(),
                probabilities.getRelaxed()
        );
    }

    private ExpressionAnalyzeResponse toResponse(FastApiExpressionAnalyzeResponse response, String fallbackAnalysisId) {
        if (response == null || response.getEmotionProbabilities() == null) {
            throw new BaseException(ErrorCode.INVALID_FORMAT);
        }

        FastApiExpressionAnalyzeResponse.EmotionProbabilities probabilities =
                response.getEmotionProbabilities();

        ExpressionAnalyzeResponse.EmotionProbabilities responseProbabilities =
                ExpressionAnalyzeResponse.EmotionProbabilities.from(
                        probabilities.getAngry(),
                        probabilities.getHappy(),
                        probabilities.getSad(),
                        probabilities.getRelaxed()
                );

        String analysisId = response.getAnalysisId() == null
                ? fallbackAnalysisId
                : response.getAnalysisId();

        return ExpressionAnalyzeResponse.from(
                analysisId,
                response.getPredictedEmotion(),
                response.getVideoUrl(),
                response.getSummary(),
                responseProbabilities
        );
    }
}
